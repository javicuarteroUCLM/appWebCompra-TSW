package edu.uclm.esi.listasbe.http;

import edu.uclm.esi.listasbe.model.Invitacion;
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

        emailEliminar = emailEliminar.replace("\"", "").trim();
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
    public String addProducto(HttpServletRequest request, @RequestBody Producto producto)
            throws org.json.JSONException {
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

    @DeleteMapping("/eliminarProducto")
    public void eliminarProducto(@RequestHeader("idProducto") String idProducto) {
        if (idProducto == null || idProducto.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se proporcionó el ID del producto");
        }

        try {
            this.listaService.eliminarProducto(idProducto);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar el producto", e);
        }
    }

    @PutMapping("/editarProducto")
    public Producto editarProducto(@RequestBody Producto producto) throws org.json.JSONException {

        if (producto.getId() == null || producto.getId().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se proporcionó el ID del producto");
        }
        if (producto.getNombre() == null || producto.getNombre().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del producto no puede ser vacío");
        }
        if (producto.getNombre().length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El nombre del producto no puede tener más de 80 caracteres");
        }

        return this.listaService.editarProducto(producto);
    }

    @PutMapping("/comprarProducto")
    public Producto comprar(@RequestBody Map<String, Object> body) throws org.json.JSONException {
        if (!body.containsKey("idProducto") || !body.containsKey("udsCompradas")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La solicitud debe incluir 'idProducto' y 'udsCompradas'");
        }

        String idProducto = body.get("idProducto").toString();
        int udsCompradas;

        try {
            udsCompradas = Integer.parseInt(body.get("udsCompradas").toString());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "udsCompradas debe ser un número válido");
        }

        return listaService.comprar(idProducto, udsCompradas);
    }

    @GetMapping("/productos/{idLista}")
    public List<Producto> getProductosDeLista(@PathVariable String idLista) {
        return this.listaService.getProductosDeLista(idLista);
    }

    @PostMapping("/compartirLista")
    public String compartirLista(@RequestHeader("idLista") String idLista, @RequestHeader("token") String token,
            @RequestBody Map<String, String> body) throws org.json.JSONException {
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
        String urlCompartir;
        try {
            urlCompartir = this.listaService.generarUrlLista(idLista);
        } catch (org.json.JSONException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al compartir la lista", e);
        }

        // Crear Invitacion
        this.listaService.crearInvitacion(idLista, emailInvitado);

        // Enviar mensaje con la URL a la amiga Ana (simulación)
        // String mensaje = "Hola Ana, puedes ver la lista compartida en el siguiente
        // enlace: " + urlCompartir;
        // System.out.println("Mensaje enviado a Ana: " + mensaje);

        return urlCompartir;
    }

    @PostMapping("/aceptarInvitacion")
    public void aceptarInvitacion(@RequestHeader("idInvitacion") String idInvitacion,
            @RequestBody String estado) {
        if (idInvitacion == null || idInvitacion.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se proporcionó el ID de la invitación");
        }
        if (estado == null || estado.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se proporcionó el estado de la invitación");
        }

        this.listaService.aceptarInvitacion(idInvitacion, estado);
    }

    @GetMapping("/invitaciones")
    public List<Invitacion> getInvitaciones(@RequestHeader("token") String token) {
        if (token == null || token.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no proporcionado");
        }

        String email = this.proxy.obtenerEmailDesdeToken(token);
        if (email == null || email.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token no válido, sin email");
        }

        return this.listaService.getInvitaciones(email);
    }

    @GetMapping("/miembros")
    public List<UsuarioLista> getMiembros(@RequestHeader("idLista") String idLista) {
        if (idLista == null || idLista.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se proporcionó el ID de la lista");
        }

        return this.listaService.getMiembros(idLista);
    }
}
