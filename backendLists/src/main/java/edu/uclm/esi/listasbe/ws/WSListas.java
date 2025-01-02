package edu.uclm.esi.listasbe.ws;

import edu.uclm.esi.listasbe.dao.UsuarioListaRepository;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.model.UsuarioLista;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class WSListas extends TextWebSocketHandler {

    @Autowired
    private UsuarioListaRepository usuarioListaRepository;

    private Map<String, List<WebSocketSession>> sessionsByIdLista = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = this.getParameter(session, "email");

        // Si el usuario ya tiene una conexión activa, retornamos y no creamos una nueva
        if (activeSessions.containsKey(email)) {
            // WebSocketSession existingSession = activeSessions.get(email);
            return;
            /*
             * if (existingSession != null && existingSession.isOpen()) {
             * System.out.println("Conexión anterior cerrada: " + existingSession.getId());
             * existingSession.close(); // Cerrar la conexión anterior
             * }
             */
        }

        // Añadir la nueva conexión activa para el usuario
        activeSessions.put(email, session);
        super.afterConnectionEstablished(session);

        List<UsuarioLista> relaciones = this.usuarioListaRepository.findByUsuarioId(email);
        System.out.println("Conexiones activas: " + activeSessions.size());
        for (UsuarioLista relacion : relaciones) {
            String idLista = relacion.getLista().getId();
            sessionsByIdLista.computeIfAbsent(idLista, k -> new ArrayList<>()).add(session);
            System.out.println("Sesión " + session.getId() + " asociada a la lista " + idLista);
        }
    }

    public void notificar(String idLista, Producto producto) throws JSONException {
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
        if (interesados == null || interesados.isEmpty()) {
            System.out.println("No hay sesiones interesadas en la lista " + idLista);
            return;
        }

        System.out.println("Notificando a las sesiones interesadas en la lista " + idLista
                + " sobre la actualización del producto " + producto.getId() + ": " + interesados);

        // Crear mensaje JSON con la información del producto a enviar
        JSONObject json = new JSONObject();
        json.put("type", "actualizacionDeLista");
        json.put("action", "updateProduct"); // Campo `action` añadido correctamente
        json.put("idLista", idLista);
        json.put("producto", new JSONObject()
                .put("id", producto.getId())
                .put("nombre", producto.getNombre())
                .put("udsPedidas", producto.getUdsPedidas())
                .put("udsCompradas", producto.getUdsCompradas()));

        TextMessage message = new TextMessage(json.toString());

        for (WebSocketSession session : interesados) {
            try {
                session.sendMessage(message);
                System.out.println("Mensaje enviado a sesión " + session.getId());
            } catch (IOException e) {
                System.err.println(
                        "Error enviando mensaje WebSocket a sesión " + session.getId() + ": " + e.getMessage());
            }
        }
    }

    public void notificarEliminacion(String idLista, String idProducto) throws JSONException {
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
        if (interesados == null || interesados.isEmpty()) {
            System.out.println("No hay sesiones interesadas en la lista " + idLista);
            return;
        }

        System.out.println("Notificando a las sesiones interesadas en la lista " + idLista
                + " sobre la eliminación del producto " + idProducto + ": " + interesados);

        JSONObject json = new JSONObject();
        json.put("type", "actualizacionDeLista");
        json.put("action", "deleteProduct");
        json.put("idLista", idLista);
        json.put("idProducto", idProducto);

        TextMessage message = new TextMessage(json.toString());

        for (WebSocketSession session : interesados) {
            try {
                session.sendMessage(message);
                System.out.println("Mensaje enviado a sesión " + session.getId());
            } catch (IOException e) {
                System.err.println(
                        "Error enviando mensaje WebSocket a sesión " + session.getId() + ": " + e.getMessage());
            }
        }
    }

    public void notificarCompra(String idLista, Producto producto) throws JSONException {
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
        if (interesados == null || interesados.isEmpty()) {
            System.out.println("No hay sesiones interesadas en la lista " + idLista);
            return;
        }

        System.out.println("Notificando a las sesiones interesadas en la lista " + idLista
                + " sobre la compra del producto " + producto.getId() + ": " + interesados);

        JSONObject json = new JSONObject();
        json.put("type", "actualizacionDeLista");
        json.put("action", "buyProduct");
        json.put("idLista", idLista);
        json.put("producto", new JSONObject()
                .put("id", producto.getId())
                .put("nombre", producto.getNombre())
                .put("udsPedidas", producto.getUdsPedidas())
                .put("udsCompradas", producto.getUdsCompradas()));

        TextMessage message = new TextMessage(json.toString());

        for (WebSocketSession session : interesados) {
            try {
                session.sendMessage(message);
                System.out.println("Mensaje enviado a sesión " + session.getId());
            } catch (IOException e) {
                System.err.println(
                        "Error enviando mensaje WebSocket a sesión " + session.getId() + ": " + e.getMessage());
            }
        }
    }

    public void notificarEdicion(String idLista, Producto producto) throws JSONException {
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
        if (interesados == null || interesados.isEmpty()) {
            System.out.println("No hay sesiones interesadas en la lista " + idLista);
            return;
        }

        System.out.println("Notificando a las sesiones interesadas en la lista " + idLista
                + " sobre la edición del producto " + producto.getId() + ": " + interesados);

        JSONObject json = new JSONObject();
        json.put("type", "actualizacionDeLista");
        json.put("action", "editProduct");
        json.put("idLista", idLista);
        json.put("producto", new JSONObject()
                .put("id", producto.getId())
                .put("nombre", producto.getNombre())
                .put("udsPedidas", producto.getUdsPedidas())
                .put("udsCompradas", producto.getUdsCompradas()));

        TextMessage message = new TextMessage(json.toString());

        for (WebSocketSession session : interesados) {
            try {
                session.sendMessage(message);
                System.out.println("Mensaje enviado a sesión " + session.getId());
            } catch (IOException e) {
                System.err.println(
                        "Error enviando mensaje WebSocket a sesión " + session.getId() + ": " + e.getMessage());
            }
        }
    }

    private String getParameter(WebSocketSession session, String parameter) {
        URI uri = session.getUri();
        String query = uri.getQuery();
        String[] parametros = query.split("&");
        for (String parametro : parametros) {
            String[] partes = parametro.split("=");
            if (partes[0].equals(parameter))
                return partes[1];
        }
        return null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            System.out.println("Mensaje recibido: " + payload);

            JSONObject json = new JSONObject(payload);
            System.out.println("Tipo de mensaje: " + json.getString("type"));

            if ("actualizacionDeLista".equals(json.getString("type"))
                    && "addProduct".equals(json.getString("action"))) {
                String idLista = json.getString("idLista");
                JSONObject productoJson = json.getJSONObject("producto");
                Producto producto = new Producto();
                producto.setNombre(productoJson.getString("nombre"));
                producto.setUdsPedidas(productoJson.getInt("udsPedidas"));
                producto.setUdsCompradas(productoJson.getInt("udsCompradas"));
                System.out.println("Producto añadido: " + producto + " a la lista " + idLista);
                this.notificar(idLista, producto);
            } else {
                System.out.println("Mensaje recibido sin 'action': " + payload);
            }
        } catch (JSONException e) {
            System.err.println("Error procesando mensaje WebSocket: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Conexión cerrada: " + session.getId());
        sessionsByIdLista.values().forEach(sessions -> sessions.remove(session));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        this.handleTextMessage(session, (TextMessage) message);
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
    }

}
