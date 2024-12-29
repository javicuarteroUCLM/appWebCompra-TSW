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
    private static ListaDao listaDao;

    public void setListaDao(ListaDao listaDao) {
        WSListas.listaDao = listaDao;
    }

    private Map<String, List<WebSocketSession>> sessionsByIdLista = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println(session.getId());
        String email = this.getParameter(session, "email");

        List<String> listas = this.listaDao.getListasDe(email);

        for (String idLista : listas) {
            List<WebSocketSession> auxiliar = this.sessionsByIdLista.get(idLista);
            if (auxiliar == null) {
                auxiliar = new ArrayList<>();
            }
            auxiliar.add(session);
            this.sessionsByIdLista.put(idLista, auxiliar);
        }
    }

    /*
     * public void notificar(String idLista, Producto producto) throws JSONException
     * {
     * List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
     * if (interesados == null)
     * return;
     * 
     * JSONObject json = new JSONObject();
     * json.put("type", "actualizacionDeLista");
     * try {
     * json.put("idLista", idLista);
     * } catch (JSONException e) {
     * e.printStackTrace();
     * }
     * json.put("udsCompradas", producto.getUdsCompradas());
     * json.put("udsPedidas", producto.getUdsPedidas());
     * json.put("nombre", producto.getNombre());
     * 
     * TextMessage message = new TextMessage(json.toString());
     * 
     * for (WebSocketSession target : interesados) {
     * new Thread(new Runnable() {
     * 
     * @Override
     * public void run() {
     * try {
     * target.sendMessage(message);
     * } catch (IOException e) {
     * 
     * }
     * }
     * }).start();
     * }
     * }
     */

    public void notificar(String idLista, Producto producto) throws JSONException {
        List<WebSocketSession> interesados = this.sessionsByIdLista.get(idLista);
        if (interesados == null || interesados.isEmpty()) {
            return; // No hay sesiones interesadas
        }

        JSONObject json = new JSONObject();
        json.put("type", "actualizacionDeLista");
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
            } catch (IOException e) {
                System.err.println("Error enviando mensaje WebSocket: " + e.getMessage());
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

    private void difundir(JSONObject json) {
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
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
