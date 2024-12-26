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
import org.springframework.stereotype.Service;

@Service
public class ProxyBEU {

    /**
     * Validar un token con el backend de usuarios.
     */
    public boolean validar(String token) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut("http://localhost:9000/tokens/validar");
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
        // Simula una llamada al servicio de validación de tokens
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:9000/tokens/obtenerEmail?token=" + token);
    
            try (CloseableHttpResponse response = httpclient.execute(httpGet)) {
                if (response.getCode() == 200) {
                    return EntityUtils.toString(response.getEntity());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    

    public boolean verificarUsuarioPagado(String email) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet("http://localhost:9000/users/esPagado?email=" + email);
    
            HttpContext context = new BasicHttpContext();
    
            try (CloseableHttpResponse response = httpclient.execute(httpGet, context)) {
                System.out.println("Response status: " + response.getCode());
                return response.getCode() == 200; // HTTP 200 indica que el usuario ha pagado
            }
    
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
}
