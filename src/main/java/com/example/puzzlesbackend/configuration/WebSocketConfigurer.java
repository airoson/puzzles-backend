package com.example.puzzlesbackend.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@Slf4j
@EnableWebSocketMessageBroker
public class WebSocketConfigurer implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/pzl-game")
                //.addInterceptors(interceptor)
                .setAllowedOrigins("http://localhost:3000", "http://localhost", "http://localhost:3001")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/game");
        //registry.enableSimpleBroker("/user/queue/updates");
        //registry.enableSimpleBroker("/user/queue/state", "user/queue/updates");
        registry.setUserDestinationPrefix("/user/queue");
    }
}
