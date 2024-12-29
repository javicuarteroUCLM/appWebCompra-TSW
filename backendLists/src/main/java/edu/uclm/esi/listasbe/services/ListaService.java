package edu.uclm.esi.listasbe.services;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.dao.ProductoDao;
import edu.uclm.esi.listasbe.dao.RestriccionUsuarioRepository;
import edu.uclm.esi.listasbe.dao.UsuarioListaRepository;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.model.UsuarioLista;
import edu.uclm.esi.listasbe.ws.WSListas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ListaService {

	@Autowired
	private ListaDao listaDao;

	@Autowired
	private ProductoDao productoDao;

	@Autowired
	private UsuarioListaRepository usuarioListaRepository;

	@Autowired
	private RestriccionUsuarioRepository restriccionUsuarioRepository;

	@Autowired
	private ProxyBEU proxy;

	@Autowired
	private WSListas wsListas;

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
	public String crearLista(String nombre, String token) {
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

	/**
	 * Actualizar las unidades compradas de un producto
	 */
	public Producto comprar(String idProducto, float udsCompradas) {
		Optional<Producto> optProducto = productoDao.findById(idProducto);
		if (optProducto.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"No se ha encontrado el producto con id " + idProducto);
		}

		Producto producto = optProducto.get();
		producto.setUdsCompradas(udsCompradas);
		productoDao.save(producto);

		return producto;
	}

	public Lista addProducto(String idLista, Producto producto, String token) throws org.json.JSONException {
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
			if (lista.getUsuarios().size() > 1) {
				throw new ResponseStatusException(HttpStatus.FORBIDDEN,
						"Los usuarios gratuitos solo pueden compartir listas con una persona");
			}
		}

		// Añadir el producto
		producto.setLista(lista);
		this.productoDao.save(producto);

		// Notificar a otros usuarios de la lista mediante WebSocket
		try {
			this.wsListas.notificar(idLista, producto);
		} catch (org.json.JSONException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al notificar a los usuarios", e);
		}

		return lista;
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
	public void borrarLista(String idLista, String token) {
		Optional<Lista> optLista = listaDao.findById(idLista);
		if (optLista.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la lista con id " + idLista);
		}

		Lista lista = optLista.get();
		String email = this.proxy.obtenerEmailDesdeToken(token);
		if (email == null || email.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
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

	public void compartirLista(String idLista, String urlCompartir) {
		UsuarioLista propietario = this.propietario(idLista);

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
	}

	public UsuarioLista propietario(String idLista) {
		UsuarioLista propietario = usuarioListaRepository.findByListaIdAndEsPropietario(idLista, true);
		if (propietario == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado el propietario de la lista");
		}
		return propietario;
	}

	public void crearInvitacion(String idLista, String emailInvitado) {
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

		// Crear la relación
		UsuarioLista usuarioLista = new UsuarioLista(emailInvitado, lista, false);
		usuarioListaRepository.save(usuarioLista);

		// Si es por WebSocket, notificar al usuario y propietario aqui

	}
}
