package edu.uclm.esi.listasbe.ws;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

@Configuration
@EnableWebSocket
public class WSConfigurer implements WebSocketConfigurer {
    private final WSListas wsListas;

    public WSConfigurer(WSListas wsListas) {
        this.wsListas = wsListas;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(wsListas, "/wsListas").setAllowedOrigins("*")
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }
}
