package edu.uclm.esi.listasbe.services;

import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PutMapping;

@Service
public class ProxyBEU {

    @PutMapping("/validar")
    public boolean validar(String token) {

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPut httpPut = new HttpPut("http://localhost:9000/tokens/validar");

            httpPut.setEntity(new StringEntity(token));
            httpPut.setHeader("Content-type", "text/plain");

            HttpContext context = new BasicHttpContext();

            try (CloseableHttpResponse response = httpclient.execute(httpPut, context)) {
                System.out.println("Response status: " + response.getCode());
                return response.getCode() == 200;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
