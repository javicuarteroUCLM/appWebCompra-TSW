package edu.uclm.esi.listasbe.ws;

import edu.uclm.esi.listasbe.dao.ListaDao;
import edu.uclm.esi.listasbe.model.Producto;
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
    private ListaDao listaDao;

    private Map<String, List<WebSocketSession>> sessionsByIdLista = new ConcurrentHashMap<>();

    /* 
    public void setListaDao(ListaDao listaDao) {
        WSListas.listaDao = listaDao;
    }  */

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String email = this.getParameter(session, "email");
        System.out.println("Email del usuario conectado: " + email);

        List<String> listas = this.listaDao.getListasDe(email);
        System.out.println("Listas asociadas al usuario " + email + ": " + listas);

        for (String idLista : listas) {
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
    
        System.out.println("Notificando a las sesiones interesadas en la lista " + idLista + ": " + interesados);
    
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
                System.err.println("Error enviando mensaje WebSocket a sesión " + session.getId() + ": " + e.getMessage());
            }
        }
    }
    
    public void notificarEliminacion(String idLista, String idProducto) throws JSONException {
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
        if (interesados == null || interesados.isEmpty()) {
            System.out.println("No hay sesiones interesadas en la lista " + idLista);
            return;
        }

        System.out.println("Notificando a las sesiones interesadas en la lista " + idLista + " sobre la eliminación del producto " + idProducto + ": " + interesados);

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
                System.err.println("Error enviando mensaje WebSocket a sesión " + session.getId() + ": " + e.getMessage());
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
        JSONObject json = new JSONObject(payload);

        // Manejar mensajes con el campo "action" (esto creo que lo voy a quitar pero ahora mismo funciona asi que NO TOCAR)
        if (json.has("action")) {
            String action = json.getString("action");

            if ("addProducto".equals(action)) {
                String idLista = json.getString("idLista");
                Producto producto = new Producto();
                producto.setId(json.getString("idProducto"));
                producto.setNombre(json.getString("nombre"));
                producto.setUdsPedidas(json.getInt("udsPedidas"));
                producto.setUdsCompradas(json.getInt("udsCompradas"));

                // Lógica para agregar el producto a la lista
                // ...

                // Notificar a los interesados
                notificar(idLista, producto);
            }
            // Manejar futuras acciones
        } else {
            // Ignorar mensajes sin el campo "action" si no son relevantes
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
