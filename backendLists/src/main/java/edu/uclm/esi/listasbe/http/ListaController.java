package edu.uclm.esi.listasbe.http;

import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.services.ListaService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;











@RestController
@RequestMapping("listas")
@CrossOrigin("*") // origins = "localhost:Portserver" allow credentials = "true"
public class ListaController {
	@Autowired
	private ListaService listaService;

	@GetMapping("/getLista")
	public List<Lista> getLista(@RequestParam String email) {
		return null;
	}

	@PostMapping("/crearLista")
	public Lista crearLista(@RequestBody Map<String, String> requestBody, @RequestHeader("token") String token) {
		// Imprimir el token recibido
		System.out.println("Token recibido: " + token);

		// Imprimir el contenido del cuerpo de la solicitud
		System.out.println("Cuerpo de la solicitud: " + requestBody);

		String nombre = requestBody.get("nombre");
		if (nombre == null || nombre.trim().isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la lista no puede estar vacío");
		}

		if (nombre.length() > 80) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la lista no puede tener más de 80 caracteres");
		}

		return this.listaService.crearLista(nombre, token);
	}


	@PostMapping("/addProducto")
	public Lista addProducto(HttpServletRequest request, @RequestBody Producto producto) {

		if (producto.getNombre().isEmpty())
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto no puede ser vacío");

		if (producto.getNombre().length() > 80)
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
					"El nombre del producto no puede tener más de 80 caracteres");

		String idLista = request.getHeader("idLista");

		return this.listaService.addProducto(idLista, producto);
	}

	@PutMapping("/comprar")
	public Producto comprar(@RequestBody Map<String, Object> compra) {

		String idProducto = compra.get("idProducto").toString();
		float udsCompradas = (float) compra.get("udsCompradas");

		return this.listaService.comprar(idProducto, udsCompradas);
	}
}
