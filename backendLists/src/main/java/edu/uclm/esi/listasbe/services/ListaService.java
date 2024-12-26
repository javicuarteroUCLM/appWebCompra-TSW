package edu.uclm.esi.listasbe.services;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.dao.ProductoDao;
import edu.uclm.esi.listasbe.dao.RestriccionUsuarioRepository;
import edu.uclm.esi.listasbe.dao.UsuarioListaRepository;
import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.model.UsuarioLista;
import edu.uclm.esi.listasbe.ws.WSListas;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    public Lista crearLista(String nombre, String token) {
		// Validar el token y obtener el email
		String email = this.proxy.obtenerEmailDesdeToken(token);
		//System.out.println("Token recibido: " + token);
		//System.out.println("Email recuperado: " + email);

		if (email == null || email.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");
		}
	
		// Verificar cuántas listas tiene el usuario
		List<Lista> listasDelUsuario = this.getListas(email);
	
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
	
		return lista;
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

	public Lista addProducto(String idLista, Producto producto) {
		Optional<Lista> optLista = this.listaDao.findById(idLista);
		if (optLista.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la lista con id " + idLista);
		}
	
		Lista lista = optLista.get();
		producto.setLista(lista);
		this.productoDao.save(producto);
	
		// Notificar cambios usando WebSocket
		this.wsListas.notificar(idLista, producto);
	
		return lista;
	}
	
}
