package edu.uclm.esi.listasbe.ws;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WSConfigurer implements WebSocketConfigurer {

    private final WSListas wsListas;

    @Autowired
    public WSConfigurer(WSListas wsListas) {
        this.wsListas = wsListas;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsListas, "/wsListas").setAllowedOrigins("*");
    }
}

