package edu.uclm.esi.listasbe.http;

import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.services.ListaService;
import edu.uclm.esi.listasbe.services.ProxyBEU;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;











@RestController
@RequestMapping("listas")
@CrossOrigin //(origins = "*", allowCredentials = "true")
public class ListaController {
    @Autowired
    private ListaService listaService;

    @Autowired
    private ProxyBEU proxy;

    @GetMapping("/getLista")
    public List<Lista> getLista(@RequestHeader("token") String token) {

		System.out.println("Token recibido en /getLista: " + token);
		String email = this.proxy.obtenerEmailDesdeToken(token);
    	System.out.println("Email recuperado: " + email);
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado");
        }
        List<Lista> listas = this.listaService.getListas(email);
    	System.out.println("Listas recuperadas: " + listas);
    
    	return listas;
    }

    @PostMapping("/crearLista")
    public Lista crearLista(@RequestBody Map<String, String> requestBody, @RequestHeader("token") String token) {
        String nombre = requestBody.get("nombre");
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre de la lista no puede ser vacío");
        }
        if (nombre.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre de la lista no puede tener más de 80 caracteres");
        }

        return this.listaService.crearLista(nombre, token);
    }

    @PostMapping("/addProducto")
    public Lista addProducto(HttpServletRequest request, @RequestBody Producto producto) {
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto no puede ser vacío");
        }
        if (producto.getNombre().length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del producto no puede tener más de 80 caracteres");
        }

        String idLista = request.getHeader("idLista");
        if (idLista == null || idLista.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se proporcionó el ID de la lista");
        }

        // Solo pasamos `idLista` y `producto`, ya que el token no se maneja aquí
        return this.listaService.addProducto(idLista, producto);
    }

    @PutMapping("/comprar")
    public Producto comprar(@RequestBody Map<String, Object> compra) {
        if (!compra.containsKey("idProducto") || !compra.containsKey("udsCompradas")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La petición debe incluir idProducto y udsCompradas");
        }

        String idProducto = compra.get("idProducto").toString();
        float udsCompradas;
        try {
            udsCompradas = Float.parseFloat(compra.get("udsCompradas").toString());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "udsCompradas debe ser un número válido");
        }

        // Solo pasamos `idProducto` y `udsCompradas`, ya que el token no se maneja aquí
        return this.listaService.comprar(idProducto, udsCompradas);
    }


}
