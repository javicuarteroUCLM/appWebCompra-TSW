package edu.uclm.esi.listasbe.services;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
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
    public boolean validar(String token) {
        String url = this.manager.getConfiguration().getString("urlBESeguro")
                + "tokens/validar";
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut(url);
            httpPut.setEntity(new StringEntity(token));
            httpPut.setHeader("Content-type", "text/plain");

            System.out.println("Enviando token para validación: " + token);

            try (CloseableHttpResponse response = httpclient.execute(httpPut)) {
                System.out.println("Código de respuesta: " + response.getCode());
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
    public String obtenerEmailDesdeToken(String token) {
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

    public boolean verificarUsuarioPagado(String email) {
        String url = this.manager.getConfiguration().getString("urlBESeguro")
                + "users/esPagado?email=" + email;
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(url);

            HttpContext context = new BasicHttpContext();

            try (CloseableHttpResponse response = httpclient.execute(httpGet, context)) {
                System.out.println("Response status: " + response.getCode());
                return response.getCode() == 200;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
