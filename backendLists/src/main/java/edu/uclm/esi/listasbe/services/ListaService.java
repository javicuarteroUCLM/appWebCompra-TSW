package edu.uclm.esi.listasbe.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.listasbe.dao.InvitacionDao;
import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.dao.ProductoDao;
import edu.uclm.esi.listasbe.dao.UsuarioListaRepository;
import edu.uclm.esi.listasbe.model.Invitacion;
import edu.uclm.esi.listasbe.model.Invitacion.EstadoInvitacion;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.model.UsuarioLista;
import edu.uclm.esi.listasbe.ws.WSListas;








@Service
public class ListaService {

	@Autowired
	private ListaDao listaDao;

	@Autowired
	private ProductoDao productoDao;

	@Autowired
	private UsuarioListaRepository usuarioListaRepository;

	@Autowired
	private InvitacionDao invitacionDao;

	@Autowired
	private ProxyBEU proxy;

	@Autowired
	private WSListas wsListas;

	private final Manager manager;

	@Autowired
	public ListaService(Manager manager) throws org.json.JSONException {
		this.manager = manager;
	}

	/**
	 * Obtener las listas asociadas a un usuario
	 */
	public List<Lista> getListas(String email) {
		List<UsuarioLista> relaciones = usuarioListaRepository.findByUsuarioId(email);
		List<Lista> listas = new ArrayList<>();

		// Extraer las listas asociadas
		for (UsuarioLista relacion : relaciones) {
			listas.add(relacion.getLista());
		}

		return listas;
	}

	/**
	 * Crear una lista nueva y asociarla al usuario que la creó.
	 */
	public String crearLista(String nombre, String token) throws org.json.JSONException {
		// Validar el token y obtener el email
		String email = this.proxy.obtenerEmailDesdeToken(token);

		if (email == null || email.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
		}

		// Verificar cuántas listas tiene el usuario
		List<Lista> listasDelUsuario = this.getListas(email);
		System.out.println("Listas del usuario: " + listasDelUsuario.size());

		if (listasDelUsuario.size() >= 2) {
			// Verificar si el usuario ha pagado
			boolean esPagado = this.proxy.verificarUsuarioPagado(email);

			if (!esPagado) {
				throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
						"Debes pagar para crear más de 2 listas.");
			}
		}

		// Crear y guardar la nueva lista
		Lista lista = new Lista();
		lista.setNombre(nombre);
		lista.setCompartida(false);
		lista.setMaxUsuarios(1); // Configuración por defecto

		this.listaDao.save(lista);
		System.out.println("Lista guardada: " + lista);

		// Asociar la lista al usuario en UsuarioLista
		UsuarioLista usuarioLista = new UsuarioLista(email, lista, true);
		this.usuarioListaRepository.save(usuarioLista);

		return lista.getId();
	}

	public Producto comprar(String idProducto, int udsCompradas) throws org.json.JSONException {
		Optional<Producto> optProducto = productoDao.findById(idProducto);
		if (optProducto.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado con ID: " + idProducto);
		}

		Producto producto = optProducto.get();
		float cantidadAntes = producto.getUdsCompradas();
		/*
		 * if (udsCompradas > producto.getUdsPedidas()) {
		 * throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
		 * "No puedes marcar más unidades de las pedidas");
		 * }
		 */

		producto.setUdsCompradas(udsCompradas + cantidadAntes);
		productoDao.save(producto);

		// Notificar a los usuarios interesados
		wsListas.notificarUpdateProduct(producto.getLista().getId(), producto);

		return producto;
	}

	public String addProducto(String idLista, Producto producto, String token) throws org.json.JSONException {
		String email = this.proxy.obtenerEmailDesdeToken(token);
		if (email == null || email.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
		}

		Optional<Lista> optLista = this.listaDao.findById(idLista);
		if (optLista.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada");
		}

		Lista lista = optLista.get();

		// Verificar restricciones para usuarios gratuitos
		boolean esPagado = this.proxy.verificarUsuarioPagado(email);
		if (!esPagado) {
			if (lista.getProductos().size() >= 10) {
				throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED,
						"Los usuarios gratuitos solo pueden añadir hasta 10 productos");
			}
			if (lista.getUsuarios().size() > 2) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Los usuarios gratuitos solo pueden compartir listas con una persona");
			}
		}

		// Añadir el producto
		producto.setLista(lista);
		this.productoDao.save(producto);

		wsListas.notificarAddProduct(idLista, producto);

		/*
		 * // Notificar a otros usuarios de la lista mediante WebSocket
		 * try {
		 * this.wsListas.notificar(idLista, producto);
		 * } catch (org.json.JSONException e) {
		 * throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
		 * "Error al notificar a los usuarios", e);
		 * }
		 */

		return producto.getId();
	}

	public void eliminarProducto(String idProducto) {
		Optional<Producto> optProducto = productoDao.findById(idProducto);
		if (optProducto.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
		}

		Producto producto = optProducto.get();
		String idLista = producto.getLista().getId();
		productoDao.delete(producto);

		// Notificar a otros usuarios de la lista mediante WebSocket
		wsListas.notificarEliminacion(idLista, idProducto);
	}

	public Optional<Lista> getListaById(String id) {
		return this.listaDao.findById(id);
	}

	public List<Producto> getProductosDeLista(String idLista) {
		Optional<Lista> optLista = listaDao.findById(idLista);
		if (optLista.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada con id " + idLista);
		}

		return optLista.get().getProductos();
	}

	/**
	 * Borrar una lista
	 */
	public void borrarLista(String idLista, String token) throws org.json.JSONException {
		Optional<Lista> optLista = listaDao.findById(idLista);
		if (optLista.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la lista con id " + idLista);
		}

		Lista lista = optLista.get();
		String email = this.proxy.obtenerEmailDesdeToken(token);
		if (email == null || email.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token propietario no válido");
		}

		// Verificar que el usuario es propietario de la lista
		UsuarioLista relacion = usuarioListaRepository.findByUsuarioIdAndListaId(email, idLista);
		if (relacion == null || !relacion.isEsPropietario()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para borrar esta lista");
		}

		// Borrar la lista
		listaDao.delete(lista);
	}

	public void borrarUsuarioDeLista(String idLista, String email) {
		// Buscar la relación entre el usuario y la lista
		System.out.println("Buscando relación entre " + email + " y " + idLista);

		UsuarioLista relacion = usuarioListaRepository.findByUsuarioIdAndListaId(email, idLista);
		if (relacion == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la relación usuario-lista");
		}

		// Borrar la relación
		usuarioListaRepository.delete(relacion);
	}

	public UsuarioLista getRelacionUsuarioLista(String email, String idLista) {
		// Buscar la relación entre el usuario y la lista
		UsuarioLista relacion = usuarioListaRepository.findByUsuarioIdAndListaId(email, idLista);
		if (relacion == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la relación usuario-lista");
		}
		return relacion;
	}

	public String generarUrlLista(String idLista) throws JSONException {
		UsuarioLista propietario = this.propietario(idLista);
		String urlCompartir = this.manager.getConfiguration().getString("urlCompartirLista")
				+ idLista;
		listaDao.findById(idLista).ifPresent(lista -> {
			lista.setCompartida(true);
			lista.setUrlInvitacion(urlCompartir);
			if (propietario.isEsPropietario()) {
				// Comprobar que el user es premium
				if (this.proxy.verificarUsuarioPagado(propietario.getUsuarioId())) {
					lista.setMaxUsuarios(1000000000);
				}
			}
			listaDao.save(lista);
		});
		return urlCompartir;
	}

	public UsuarioLista propietario(String idLista) {
		UsuarioLista propietario = usuarioListaRepository.findByListaIdAndEsPropietario(idLista, true);
		if (propietario == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado el propietario de la lista");
		}
		return propietario;
	}

	public void crearInvitacion(String idLista, String emailInvitado, String urlCompartir) throws org.json.JSONException {
		// Obtener la lista
		Optional<Lista> optLista = listaDao.findById(idLista);
		if (optLista.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la lista con id " + idLista);
		}

		Lista lista = optLista.get();

		// Verificar si el usuario ya está en la lista
		UsuarioLista relacion = usuarioListaRepository.findByUsuarioIdAndListaId(emailInvitado, idLista);
		if (relacion != null) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "El usuario ya está en la lista");
		}

		// Verificar si la lista está llena
		if (lista.getUsuarios().size() - 1 >= lista.getMaxUsuarios()) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "La lista está llena");
		}

		// Crear la invitación
		Invitacion invitacion = new Invitacion(lista, emailInvitado, EstadoInvitacion.PENDIENTE);
		this.invitacionDao.save(invitacion);

		// Notificar al usuario por Correo

		// Crear contenido HTML por mensaje String para enviarlo al email
		String mensaje = "<h1>¡Hola!</h1><p>Has sido invitado a la lista " + lista.getNombre()
				+ "</p><p>Para aceptar la invitación, haz clic en el siguiente enlace: <a href=\""
				+ urlCompartir + "\">Aceptar invitación</a></p>";
		System.out.println("Mensaje: " + mensaje);
		this.proxy.enviarEmail(emailInvitado, lista.getNombre(), mensaje);

		try {
			this.proxy.enviarEmail(emailInvitado, "Invitacion:" + lista.getNombre(), urlCompartir);
		} catch (org.json.JSONException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al enviar el email", e);
		}
	}

	public void aceptarInvitacion(String idInvitacion, String estado) {
		estado = estado.toLowerCase();

		// Obtener la invitación
		Optional<Invitacion> optInvitacion = invitacionDao.findById(idInvitacion);
		if (optInvitacion.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"No se ha encontrado la invitación con id " + idInvitacion);
		}

		Invitacion invitacion = optInvitacion.get();
		if (invitacion.getEstado() != EstadoInvitacion.PENDIENTE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La invitación ya ha sido aceptada o rechazada");
		}

		if (!estado.equals("aceptado") && !estado.equals("rechazado")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de invitación no válido");
		}

		if (estado.equals("rechazado")) {
			invitacion.setEstado(EstadoInvitacion.RECHAZADA);
			invitacionDao.save(invitacion);
			// Notificar al propietario de la lista por WebSocket ?
			return;
		}

		// Cambiar el estado de la invitación
		invitacion.setEstado(EstadoInvitacion.ACEPTADA);
		invitacionDao.save(invitacion);

		// Crear la relación
		UsuarioLista usuarioLista = new UsuarioLista(invitacion.getEmailInvitado(), invitacion.getLista(), false);
		usuarioListaRepository.save(usuarioLista);
	}

	// Método para editar un producto dado el objeto Producto entero
	public Producto editarProducto(Producto producto) throws org.json.JSONException {
		Optional<Producto> optProducto = productoDao.findById(producto.getId());
		if (optProducto.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"No se ha encontrado el producto con id " + producto.getId());
		}

		String idLista = optProducto.get().getLista().getId();

		Producto productoGuardado = optProducto.get();
		productoGuardado.setNombre(producto.getNombre());
		productoGuardado.setUdsPedidas(producto.getUdsPedidas());
		productoGuardado.setUdsCompradas(producto.getUdsCompradas());
		productoDao.save(productoGuardado);

		// Notificar a los usuarios interesados
		wsListas.notificarUpdateProduct(idLista, producto);
		return productoGuardado;
	}

	public List<UsuarioLista> getMiembros(String idLista) {
		return usuarioListaRepository.findByListaId(idLista);
	}

	public List<Invitacion> getInvitaciones(String email) {
		return invitacionDao.findByEmailInvitado(email);
	}

}
