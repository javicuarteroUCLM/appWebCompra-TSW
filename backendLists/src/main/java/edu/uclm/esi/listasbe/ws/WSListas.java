package edu.uclm.esi.listasbe.ws;

import edu.uclm.esi.listasbe.dao.UsuarioListaRepository;
import edu.uclm.esi.listasbe.model.Producto;
import edu.uclm.esi.listasbe.model.UsuarioLista;
import edu.uclm.esi.listasbe.services.ProxyBEU;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    ProxyBEU proxy;

    private Map<String, List<WebSocketSession>> sessionsByIdLista = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, WebSocketSession> activeSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        //String email = this.getParameter(session, "email");
        //System.out.println("Conexión establecida: " + session.getId() + " para el usuario " + email);
        String token = this.getParameter(session, "token");
        String email = proxy.obtenerEmailDesdeToken(token);
        // Si el usuario ya tiene una conexión activa, retornamos y no creamos una nueva
        
        if (activeSessions.containsKey(email)) {
            System.out.println("El usuario " + email + " ya tiene una conexión activa");
            session.close();
            return;
        }

        if (email == null) {
            session.close();
            System.out.println("No se ha podido obtener el email del token");
            throw new Exception("Token no válido o no proporcionado");
        }

        System.out.println("Conexión establecida: " + session.getId() + " para el usuario " + email);
        // Añadir la nueva conexión activa para el usuario
        activeSessions.put(email, session);
        super.afterConnectionEstablished(session);

        List<UsuarioLista> relaciones = this.usuarioListaRepository.findByUsuarioId(email);
        System.out.println("Conexiones activas: " + activeSessions.size());
        for (UsuarioLista relacion : relaciones) {
            String idLista = relacion.getLista().getId();
            List<WebSocketSession> sesiones = sessionsByIdLista.computeIfAbsent(idLista, k -> new ArrayList<>());

            // Evitar sesiones duplicadas para la misma lista
            if (!sesiones.contains(session)) {
                sesiones.add(session);
                System.out.println("Sesión " + session.getId() + " asociada a la lista " + idLista);
            }
        }
    }

    public void broadcastUpdate(String idLista, String action, Map<String, Object> payload) {
        // Obtener las sesiones interesadas en esta lista
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
    
        if (interesados == null || interesados.isEmpty()) {
            System.out.println("No hay sesiones interesadas en la lista " + idLista);
            return;
        }
    
        System.out.println("Enviando actualización a las sesiones interesadas en la lista " + idLista);
    
        // Crear mensaje JSON con la acción y los datos del payload
        JSONObject json = new JSONObject();
        try {
            json.put("type", "actualizacionDeLista");
            json.put("action", action);
            json.put("idLista", idLista);
    
            // Agregar todos los datos del payload al mensaje JSON
            for (Map.Entry<String, Object> entry : payload.entrySet()) {
                json.put(entry.getKey(), entry.getValue());
            }
    
            TextMessage message = new TextMessage(json.toString());
    
            // Enviar el mensaje a todas las sesiones interesadas
            for (WebSocketSession session : interesados) {
                try {
                    session.sendMessage(message);
                    System.out.println("Mensaje enviado a sesión " + session.getId());
                } catch (IOException e) {
                    System.err.println("Error enviando mensaje WebSocket a sesión " + session.getId() + ": " + e.getMessage());
                }
            }
        } catch (JSONException e) {
            System.err.println("Error creando el mensaje JSON para broadcastUpdate: " + e.getMessage());
        }
    }
    

    public void notificarAddProduct(String idLista, Producto producto) throws JSONException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("producto", new JSONObject()
                .put("id", producto.getId())
                .put("nombre", producto.getNombre())
                .put("udsPedidas", producto.getUdsPedidas())
                .put("udsCompradas", producto.getUdsCompradas()));

        broadcastUpdate(idLista, "addProduct", payload);

    }

    public void notificarUpdateProduct(String idLista, Producto producto) throws JSONException {
        Map<String, Object> payload = new HashMap<>();
        payload.put("producto", new JSONObject()
                .put("id", producto.getId())
                .put("nombre", producto.getNombre())
                .put("udsPedidas", producto.getUdsPedidas())
                .put("udsCompradas", producto.getUdsCompradas()));

        broadcastUpdate(idLista, "updateProduct", payload);
    }

    public void notificarEliminacion(String idLista, String idProducto) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("idProducto", idProducto);

        // Usa broadcastUpdate para notificar a todas las sesiones interesadas
        broadcastUpdate(idLista, "deleteProduct", payload);
    }

    private JSONObject crearJSON(String action, String idLista, Producto producto) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("type", "actualizacionDeLista");
        json.put("action", action);
        json.put("idLista", idLista);
        json.put("producto", new JSONObject()
                .put("id", producto.getId())
                .put("nombre", producto.getNombre())
                .put("udsPedidas", producto.getUdsPedidas())
                .put("udsCompradas", producto.getUdsCompradas()));
        return json;
    }

    private List<WebSocketSession> comprobarInteresados(String idLista) {
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
        if (interesados == null || interesados.isEmpty()) {
            System.out.println("No hay sesiones interesadas en la lista " + idLista);
            return null;
        }
        return interesados;
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
            if ("actualizacionDeLista".equals(json.getString("type"))) {
                String action = json.getString("action");
                String idLista = json.getString("idLista");
                switch (action) {
                    case "addProduct":
                        Producto producto = obtenerProducto(json);
                        this.notificarAddProduct(idLista, producto);
                        break;
                    case "updateProduct":
                        Producto productoUpdate = obtenerProducto(json);
                        this.notificarUpdateProduct(idLista, productoUpdate);
                        break;
                    case "deleteProduct":
                        String idProducto = json.getString("idProducto");
                        this.notificarEliminacion(idLista, idProducto);
                        break;
                    default:
                        System.out.println("Mensaje recibido sin 'action' válida: " + payload);
                        break;
                }
            }
        } catch (JSONException e) {
            System.err.println("Error procesando mensaje WebSocket: " + e.getMessage());
        }
    }

    private Producto obtenerProducto(JSONObject json) throws JSONException {
        JSONObject productoJson = json.getJSONObject("producto");
        Producto producto = new Producto();
        producto.setNombre(productoJson.getString("nombre"));
        producto.setUdsPedidas(productoJson.getInt("udsPedidas"));
        producto.setUdsCompradas(productoJson.getInt("udsCompradas"));
        return producto;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Conexión cerrada: " + session.getId());
        sessionsByIdLista.values().forEach(sessions -> sessions.remove(session));
        activeSessions.values().remove(session);
        System.out.println("Conexiones activas: " + activeSessions.size());
        System.out.println("Conexiones por lista: " + sessionsByIdLista.size());
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
