package edu.uclm.esi.listasbe.http;

import edu.uclm.esi.listasbe.model.Lista;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.model.UsuarioLista;
import edu.uclm.esi.listasbe.services.ListaService;
import edu.uclm.esi.listasbe.services.ProxyBEU;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("listas")
@CrossOrigin(origins = "http://localhost:3000", allowedHeaders = "*", allowCredentials = "true", methods = {
        RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE })
public class ListaController {
    @Autowired
    private ListaService listaService;

    @Autowired
    private ProxyBEU proxy;

    // Método para obtener las listas de un usuario
    @GetMapping("/getListas")
    public List<Lista> getLista(@RequestHeader("token") String token) {
        String email = this.proxy.obtenerEmailDesdeToken(token);
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado");
        }
        List<Lista> listas = this.listaService.getListas(email);

        return listas;
    }

    // Método para obtener las listas de un usuario
    @GetMapping("/verLista")
    public Lista verLista(@RequestHeader("idLista") String id) {
        if (id == null || id.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "ID de la lista no proporcionado");
        }
        return this.listaService.getListaById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Lista no encontrada"));
    }

    @PostMapping("/crearLista")
    public String crearLista(@RequestBody Map<String, String> requestBody, @RequestHeader("token") String token) {
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

    @DeleteMapping("/borrarLista")
    public void borrarLista(@RequestHeader("idLista") String idLista, @RequestHeader("token") String token) {
        if (idLista == null || idLista.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se proporcionó el ID de la lista");
        }

        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token no proporcionado");
        }

        this.listaService.borrarLista(idLista, token);
    }

    @DeleteMapping("/borrarUsuarioDeLista")
    public void borrarUsuarioDeLista(@RequestHeader("idLista") String idLista, @RequestHeader("token") String token,
            @RequestBody String emailEliminar) {
        if (idLista == null || idLista.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se proporcionó el ID de la lista");
        }

        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Token no proporcionado");
        }

        // Obterner email del propietario de la lista
        String emailPropietario = this.proxy.obtenerEmailDesdeToken(token);
        if (emailPropietario == null || emailPropietario.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido, sin propietario.");
        }

        // Comprobar que no se elimina a uno mismo
        if (emailPropietario.equals(emailEliminar)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes eliminarte a ti mismo de la lista");
        }

        // Comprobar si la lista existe
        if (this.listaService.getListaById(idLista).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la lista con id " + idLista);
        }

        // Comprobar si el usuario es propietario de la lista
        UsuarioLista relacion = this.listaService.getRelacionUsuarioLista(emailPropietario, idLista);
        if (relacion == null || !relacion.isEsPropietario()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No tienes permiso para borrar esta lista");
        }

        // Borrar el usuario de la lista
        this.listaService.borrarUsuarioDeLista(idLista, emailEliminar);
    }

    @PostMapping("/addProducto")
    public Lista addProducto(HttpServletRequest request, @RequestBody Producto producto) throws org.json.JSONException {
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
        String token = request.getHeader("token");
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado");
        }
        try {
            System.out.println("Solicitud para añadir producto a la lista " + idLista);
            System.out.println("Producto recibido: " + producto.getNombre());
            System.out.println("Token del usuario: " + token);
            return this.listaService.addProducto(idLista, producto, token);
        } catch (org.json.JSONException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error procesando JSON", e);
        }
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

    @GetMapping("/productos/{idLista}")
    public List<Producto> getProductosDeLista(@PathVariable String idLista) {
        return this.listaService.getProductosDeLista(idLista);
    }

    @PostMapping("/compartirLista")
    public String compartirLista(@RequestHeader("idLista") String idLista, @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) {
        String emailInvitado = body.get("emailInvitado");

        if (idLista == null || idLista.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se proporcionó el ID de la lista");
        }

        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado");
        }

        // Comprobar si la lista existe
        if (this.listaService.getListaById(idLista).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se ha encontrado la lista con id " + idLista);
        }

        // Comprobar si hay email de invitado
        if (emailInvitado == null || emailInvitado.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se proporcionó el email del invitado");
        }

        // Generar URL para compartir la lista
        String urlCompartir = this.listaService.compartirLista(idLista);

        // Crear Invitacion
        this.listaService.crearInvitacion(idLista, emailInvitado);

        // Enviar mensaje con la URL a la amiga Ana (simulación)
        String mensaje = "Hola Ana, puedes ver la lista compartida en el siguiente enlace: " + urlCompartir;
        System.out.println("Mensaje enviado a Ana: " + mensaje);

        return urlCompartir;
    }

}
