package com.example.puzzlesbackend.event;

import com.example.puzzlesbackend.services.GameService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebsocketDisconnectHandler implements ApplicationListener<SessionDisconnectEvent> {
    @Autowired
    private GameService gameService;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String gameId = gameService.getGameIdBySessionId(event.getSessionId());
        if(gameId != null){
            gameService.removeUser(gameId, event.getSessionId());
        }
    }
}
