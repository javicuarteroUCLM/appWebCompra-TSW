package edu.uclm.esi.listasbe.services;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONObject;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class Manager {
    private JSONObject configuracion;

    private Manager() {
        String s;
        try {
            s = readFileAsString(this, "configuracion.json");

            JSONObject config = new JSONObject(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject getConfiguration() {
        return configuracion;
    }

    private static class ManagerHolder {
        static Manager singleton = new Manager();
    }

    public byte[] readBinary(Object o, String fileName) throws IOException {
        ClassLoader classLoader = o.getClass().getClassLoader();
        try (InputStream fis = classLoader.getResourceAsStream(fileName)) {
            byte[] b = new byte[fis.available()];
            fis.read(b);
            return b;
        }
    }

    public String readFileAsString(Object o, String fileName) throws IOException {
        ClassLoader classLoader = o.getClass().getClassLoader();
        try (InputStream fis = classLoader.getResourceAsStream(fileName)) {
            byte[] b = new byte[fis.available()];
            fis.read(b);
            String s = new String(b);
            return s;
        }
    }

    @Bean
    public static Manager getManager() {
        return ManagerHolder.singleton;
    }
}
