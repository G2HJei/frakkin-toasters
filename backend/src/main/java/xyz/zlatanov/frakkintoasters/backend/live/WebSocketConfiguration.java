package xyz.zlatanov.frakkintoasters.backend.live;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.List;

/**
 * The game socket. The handshake checks the {@code Origin}: the same origin, and the ones in
 * {@code frakkin.allowed-origins}, e.g. the Vite dev server in the {@code dev} profile.
 */
@Configuration(proxyBeanMethods = false)
@EnableWebSocket
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
