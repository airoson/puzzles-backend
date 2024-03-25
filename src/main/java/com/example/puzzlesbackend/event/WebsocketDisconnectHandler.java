package com.example.puzzlesbackend.event;

import com.example.puzzlesbackend.dto.messages.PuzzlesState;
import com.example.puzzlesbackend.entities.GameSession;
import com.example.puzzlesbackend.entities.Puzzle;
import com.example.puzzlesbackend.services.GameService;
import com.example.puzzlesbackend.services.MessageSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;

@Component
public class WebsocketDisconnectHandler implements ApplicationListener<SessionDisconnectEvent> {
    @Autowired
    private GameService gameService;
    @Autowired
    private MessageSender messageSender;

    @Override
    public void onApplicationEvent(SessionDisconnectEvent event) {
        String gameId = gameService.getGameIdBySessionId(event.getSessionId());
        if(gameId != null){
            gameService.removeUser(gameId, event.getSessionId());
            List<String> users = gameService.getAllUsers(gameId);
            GameSession gameSession = gameService.getGameSession(gameId);
            if(gameSession.getComponents() <= 1){
                gameService.deleteGame(gameSession);
            }else{
                PuzzlesState state = new PuzzlesState();
                state.setUsers(users.size());
                state.setState(PuzzlesState.State.FINE);
                for(String user: users){
                    messageSender.sendMessage(user, "state", state);
                }
            }
        }
    }
}
