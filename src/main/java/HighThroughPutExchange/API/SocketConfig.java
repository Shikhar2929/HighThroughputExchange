package HighThroughPutExchange.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.context.annotation.Bean;

import java.security.Principal;
import java.util.concurrent.ConcurrentHashMap;


@Configuration
@EnableWebSocketMessageBroker
public class SocketConfig implements WebSocketMessageBrokerConfigurer {
    @Bean
    public SocketInterceptor socketInterceptor() {
        // Inject socketUsers into SocketInterceptor
        return new SocketInterceptor();
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/exchange-socket")
                .setAllowedOriginPatterns("*")
                .addInterceptors(socketInterceptor())
                .setHandshakeHandler(new HandshakeHandler());
    }
}
