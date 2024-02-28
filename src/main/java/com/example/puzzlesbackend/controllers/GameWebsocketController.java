package com.example.puzzlesbackend.controllers;

import com.example.puzzlesbackend.dto.messages.*;
import com.example.puzzlesbackend.entities.Puzzle;
import com.example.puzzlesbackend.services.GameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Controller
@Slf4j
public class GameWebsocketController {
    @Autowired
    private GameService gameService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private void sendToUserBySessionId(String sessionId, String queue, Object message){
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor
                .create(SimpMessageType.MESSAGE);
        headerAccessor.setSessionId(sessionId);
        headerAccessor.setLeaveMutable(true);
        messagingTemplate.convertAndSendToUser(sessionId, queue, message, headerAccessor.getMessageHeaders());
    }

    @MessageMapping("moves")
    public void updatePosition(Move update, @Header("simpSessionId") String sessionId){
        String gameId = gameService.getGameIdBySessionId(sessionId);
        if(gameId != null){
            List<Puzzle> updates = gameService.updatePosition(update.getId(), update.getX(), update.getY(), gameId);
            log.info("update {}", update);
            log.info("Updates: {}", updates);
            PuzzlesState updatesState = new PuzzlesState();
            List<PuzzleDescription> descriptions = new LinkedList<>();
            for(Puzzle p: updates){
                descriptions.add(new PuzzleDescription(p.getX(), p.getY(), p.getPuzzleId()));
            }
            updatesState.setPuzzles(descriptions);
            log.info("puzzles: {}", updatesState.getPuzzles());
            updatesState.setState(PuzzlesState.State.FINE);
            if(updatesState.getPuzzles().size() != 0)
                sendToUserBySessionId(sessionId, "updates", updatesState);
            List<String> users = gameService.getAllUsers(gameId);
            updatesState.getPuzzles().add(new PuzzleDescription(update.getX(), update.getY(), update.getId()));
            for(String user: users){
                if(user.equals(sessionId)) continue;
                log.info("Sending move {} for user {}", update, user);
                sendToUserBySessionId(user, "updates", updatesState);
            }
        }
    }

    @MessageMapping("state")
    public void getCurrentState(@Header("simpSessionId") String sessionId){
        String gameId = gameService.getGameIdBySessionId(sessionId);
        if(gameId != null){
            List<Puzzle> puzzles = gameService.getAllPuzzles(gameId);
            List<PuzzleDescription> positions = new ArrayList<>();
            for(Puzzle p: puzzles){
                positions.add(new PuzzleDescription(p.getX(), p.getY(), p.getPuzzleId(), p.getComponentId(), p.isInGame(), p.getNeighbors()));
            }
            PuzzlesState puzzlesState = new PuzzlesState();
            puzzlesState.setPuzzles(positions);
            puzzlesState.setState(PuzzlesState.State.FINE);
            log.info("Return current puzzle state: puzzles = {} puzzleState = {}", puzzles, puzzlesState);
            sendToUserBySessionId(sessionId, "state", puzzlesState);
        }else{
            PuzzlesState puzzlesState = new PuzzlesState();
            puzzlesState.setState(PuzzlesState.State.TERMINATED);
            log.info("Game session is not found for sessionId: {}", sessionId);
            sendToUserBySessionId(sessionId, "state", puzzlesState);
        }
    }

    @MessageMapping("/connects")
    public void connectPuzzles(@Header("simpSessionId") String sessionId, PuzzleConnectionRequest connect){
        String gameId = gameService.getGameIdBySessionId(sessionId);
        if(gameId != null){
            List<Puzzle> puzzles = gameService.connectPuzzles(connect.getPuzzle1Id(), connect.getPuzzle2Id(), connect.getX(), connect.getY(), gameId);
            PuzzleConnection puzzleConnection = new PuzzleConnection();
            puzzleConnection.setStatus(gameService.isGameSolved(gameId) ? PuzzleConnection.Status.SOLVED: PuzzleConnection.Status.NOT_SOLVED);
            List<ConnectDesc> connects = new LinkedList<>();
            for(Puzzle p: puzzles){
                connects.add(new ConnectDesc(p.getPuzzleId(), p.getComponentId(), p.getX(), p.getY()));
            }
            puzzleConnection.setConnects(connects);
            sendToUserBySessionId(sessionId, "/connects", puzzleConnection);
            puzzleConnection.getConnects().add(new ConnectDesc(connect.getPuzzle2Id(), !puzzles.isEmpty() ? puzzles.get(0).getComponentId() : connect.getPuzzle1Id(), connect.getX(), connect.getY()));
            List<String> users = gameService.getAllUsers(gameId);
            for(String user: users){
                if(user.equals(sessionId)) continue;
                log.info("Sending connects update to user {}. Updates: {}", user, puzzleConnection);
                sendToUserBySessionId(user, "/connects", puzzleConnection);
            }
        }else{
            PuzzlesState puzzlesState = new PuzzlesState();
            puzzlesState.setState(PuzzlesState.State.TERMINATED);
            log.info("Game session is not found for sessionId: {}", sessionId);
            sendToUserBySessionId(sessionId, "/connects", puzzlesState);
        }
    }
}
