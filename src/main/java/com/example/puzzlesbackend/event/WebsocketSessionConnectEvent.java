package com.example.puzzlesbackend.event;

import com.example.puzzlesbackend.dto.messages.PuzzlesState;
import com.example.puzzlesbackend.services.GameService;
import com.example.puzzlesbackend.services.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class WebsocketSessionConnectEvent implements ApplicationListener<SessionConnectEvent> {
    @Autowired
    private GameService gameService;
    @Autowired
    private MessageSender messageSender;

    @Override
    public void onApplicationEvent(SessionConnectEvent event) {
        var headers = event.getMessage().getHeaders();
        String gameId = ((List<String>)((Map<String, Object>)headers.get("nativeHeaders")).get("gameId")).get(0);
        String sessionId = headers.get("simpSessionId").toString();
        if(gameId != null){
            gameService.addUser(gameId, sessionId);
            List<String> users = gameService.getAllUsers(gameId);
            PuzzlesState state = new PuzzlesState();
            state.setState(PuzzlesState.State.FINE);
            state.setUsers(users.size());
            for(String user: users){
                if(user.equals(sessionId)) continue;
                messageSender.sendMessage(user, "state", state);
            }
            log.info("Add sessionId {} to gameId {}", sessionId, gameId);
        }
    }

    @Override
    public boolean supportsAsyncExecution() {
        return ApplicationListener.super.supportsAsyncExecution();
    }
}
