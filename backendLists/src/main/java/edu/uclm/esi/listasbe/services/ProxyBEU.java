package edu.uclm.esi.listasbe.services;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class ProxyBEU {

    private final Manager manager;

    @Autowired
    public ProxyBEU(Manager manager) throws org.json.JSONException {
        this.manager = manager;
    }

    /**
     * Validar un token con el backend de usuarios.
     */
    public boolean validar(String token) throws JSONException {
        String url = this.manager.getConfiguration().getString("urlBESeguro")
                + "tokens/validar";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(new StringEntity(token));
            httpPut.setHeader("Content-type", "text/plain");

            try (CloseableHttpResponse response = httpclient.execute(httpPut)) {
                return response.getCode() == 200;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtener el email del usuario asociado a un token.
     */
    public String obtenerEmailDesdeToken(String token) throws JSONException {
        this.validar(token);
        String url = this.manager.getConfiguration().getString("urlBESeguro")
                + "tokens/obtenerEmail?token=" + token;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getCode() == 200) {
                    // Extrae el email del cuerpo de la respuesta
                    org.json.JSONObject jsonObject = new org.json.JSONObject(
                            EntityUtils.toString(response.getEntity()));
                    return jsonObject.getString("email");
                } else {
                    System.err.println("Error en la validación del token: " + response.getCode());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public boolean verificarUsuarioPagado(String email) throws JSONException {
        String url = this.manager.getConfiguration().getString("urlBESeguro")
                + "users/esPagado?email=" + email;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            HttpContext context = new BasicHttpContext();

            try (CloseableHttpResponse response = httpclient.execute(httpGet, context)) {
                return response.getCode() == 200;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /* public void enviarEmail(String email, String subject, String message) throws JSONException {
        String url = this.manager.getConfiguration().getString("urlBESeguro")
                + "email/sendEmail?email=" + email + "&subject=" + subject + "&message=" + message;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);
            httpPost.setHeader("Content-type", "application/json");

            org.json.JSONObject json = new org.json.JSONObject();
            json.put("email", email);
            json.put("subject", subject);
            json.put("message", message);

            httpPost.setEntity(new StringEntity(json.toString()));
            

            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
            if (response.getCode() != 200) {
                System.err.println("Error al enviar el correo: " + response.getCode());
            }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    } */

    public void enviarEmail(String email, String subject, String message) throws JSONException {
    try {
        // Codificar los parámetros para evitar caracteres no válidos en la URL
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8.toString());
        String encodedSubject = URLEncoder.encode(subject, StandardCharsets.UTF_8.toString());
        String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8.toString());

        // Construir la URL base
        String urlBase = this.manager.getConfiguration().getString("urlBESeguro");
        String url = urlBase + "email/sendEmail?email=" + encodedEmail + "&subject=" + encodedSubject + "&message=" + encodedMessage;

        // Crear el cliente HTTP
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(url);

            // Crear el cuerpo de la solicitud en formato JSON
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("email", email);
            json.put("subject", subject);
            json.put("message", message);

            // Configurar los headers y la entidad JSON
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setEntity(new StringEntity(json.toString()));

            // Ejecutar la solicitud HTTP
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                if (response.getCode() != 200) {
                    System.err.println("Error al enviar el correo: Código de respuesta " + response.getCode());
                } else {
                    System.out.println("Correo enviado exitosamente.");
                }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
        throw new RuntimeException("Error al enviar el correo.", e);
    }
}

}
