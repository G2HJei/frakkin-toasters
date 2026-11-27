package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

@Configuration(proxyBeanMethods = false)
@EnableWebSocket
@EnableScheduling // the sweep of silent connections
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final GameSocketHandler        handler;
    private final GameHandshakeInterceptor handshake;

    @Value("${frakkin.allowed-origins:}")
    private List<String> allowedOrigins;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/games/*")
                .addInterceptors(handshake)
                .setAllowedOrigins(allowedOrigins.stream().filter(origin -> !origin.isBlank()).toArray(String[]::new));
    }

}
