package kg.kut.os.mentorhub.common.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration using STOMP over SockJS.
 * Can be disabled with property: app.websocket.enabled=false
 */
@Configuration
@EnableWebSocketMessageBroker
@ConditionalOnProperty(name = "app.websocket.enabled", havingValue = "true", matchIfMissing = true)
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed.origins:http://localhost:5173}")
    private String corsAllowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        String[] allowedOriginsArray = corsAllowedOrigins.split(",");

        registry.addEndpoint("/ws-stomp") // the endpoint frontend will connect to
                .setAllowedOriginPatterns(allowedOriginsArray) // allow CORS from configured origins
                .withSockJS(); // fallback
    }
}
