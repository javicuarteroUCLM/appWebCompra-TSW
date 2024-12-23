package edu.uclm.esi.listasbe.ws;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.json.JSONObject;

@Component
public class WSChat extends TextWebSocketHandler {

    private Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private Map<String, WebSocketSession> sessionsByNombre = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println(session.getId());
        String nombreUsuario = this.getNombreParameter(session);
        this.sessions.put(session.getId(), session);
        this.sessionsByNombre.put(nombreUsuario, session);

        JSONObject jso = new JSONObject();
        jso.put("type", "Bienvenido al chat");
        jso.put("message", nombreUsuario + " se ha conectado");
        this.difundir(jso);
    }

    private void difundir(JSONObject json) {
        for (WebSocketSession target : this.sessions.values()) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        target.sendMessage(new TextMessage(json.toString()));
                    } catch (IOException e) {
                        WSChat.this.sessions.remove(target.getId());
                    }
                }
            }).start();
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        JSONObject jso = new JSONObject(message.getPayload());
        if (jso.getString("type").equalsIgnoreCase("difusion")) {
        
            jso.put("type", "mensajeDeTexto");
            jso.put("message", jso.getString("message"));
        
        } else if (jso.getString("type").equalsIgnoreCase("mensajeParticular")) {
            String destinatario = jso.getString("destinatario");
            WebSocketSession wsDestinatario = this.sessionsByNombre.get(destinatario);
            if (wsDestinatario == null) {
                try {
                    wsDestinatario.sendMessage(message);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        // Ensure the message is only sent once
        if (!jso.has("type")) {
            jso.put("type", "message");
            jso.put("message", message.getPayload());
            this.difundir(jso);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        this.sessions.remove(session.getId());
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        this.handleTextMessage(session, (TextMessage) message);
    }

    private String getNombreParameter(WebSocketSession session) {
        URI uri = session.getUri();
        String query = uri.getQuery();
        String[] parametros = query.split("&");
        for (String parametro : parametros) {
            String[] partes = parametro.split("=");
            if (partes[0].equals("nombre"))
                return partes[1];
        }
        return null;
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception)
            throws Exception {
    }
}