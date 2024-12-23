package edu.uclm.esi.listasbe.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.ws.WSListas;
import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.dao.ProductoDao;

@Service
public class ListaService {

	@Autowired
	private ListaDao listaDao;
	@Autowired
	private ProductoDao productoDao;
	@Autowired
	private ProxyBEU proxy;
	@Autowired
	private WSListas wsListas;

	public List<Lista> getListas(String email) {
		List<Lista> result = new ArrayList<>();
		List<Lista> ids = this.listaDao.getListasDe(email);
		for (Lista id : ids) {
			result.add(this.listaDao.findById(id).get());
		}
		return result;
	}

	public Lista crearLista(String nombre, String token) {
		// boolean correcto = this.proxy.validar(token);

		if (!this.proxy.validar(token))
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido");

		Lista lista = new Lista();
		lista.setNombre(nombre);

		this.listaDao.save(lista);
		return lista;
	}

	public Lista addProducto(String idLista, Producto producto) {
		Optional<Lista> optLista = this.listaDao.findById(idLista);
		if (optLista.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la lista con id " + idLista);
		Lista lista = optLista.get();
		lista.add(producto);

		producto.setLista(lista);
		this.productoDao.save(producto);
		this.wsListas.notificar(idLista, producto);
		return lista;
	}

	public Producto comprar(String idProducto, float udsCompradas) {
		Optional<Producto> optProducto = this.productoDao.findById(idProducto);
		if (optProducto.isEmpty())
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,
					"No se ha encontrado el producto con id " + idProducto);
		Producto producto = optProducto.get();
		producto.setUdsCompradas(udsCompradas);
		this.productoDao.save(producto);
		return producto;
	}

}
