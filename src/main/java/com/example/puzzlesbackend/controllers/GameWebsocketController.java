package com.example.puzzlesbackend.controllers;

import com.example.puzzlesbackend.dto.messages.*;
import com.example.puzzlesbackend.entities.GameSession;
import com.example.puzzlesbackend.entities.Puzzle;
import com.example.puzzlesbackend.services.GameService;
import com.example.puzzlesbackend.services.MessageSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@Controller
@Slf4j
public class GameWebsocketController {
    @Autowired
    private GameService gameService;
    @Autowired
    private MessageSender messageSender;

    @MessageMapping("moves")
    public void updatePosition(Move update, @Header("simpSessionId") String sessionId){
        String gameId = gameService.getGameIdBySessionId(sessionId);
        if(gameId != null){
            List<Puzzle> updates = gameService.updatePosition(update.getId(), update.getX(), update.getY(), gameId);
            PuzzlesState updatesState = new PuzzlesState();
            List<PuzzleDescription> descriptions = new LinkedList<>();
            for(Puzzle p: updates){
                descriptions.add(new PuzzleDescription(p.getX(), p.getY(), p.getPuzzleId()));
            }
            updatesState.setPuzzles(descriptions);
            updatesState.setState(PuzzlesState.State.FINE);
            List<String> users = gameService.getAllUsers(gameId);
            for(String user: users){
                if(user.equals(sessionId)) continue;
                // log.info("Sending move {} for user {}", update, user);
                messageSender.sendMessage(user, "updates", updatesState);
            }
        }
    }

    @MessageMapping("state")
    public void getCurrentState(@Header("simpSessionId") String sessionId){
        String gameId = gameService.getGameIdBySessionId(sessionId);
        GameSession gameSession;
        if(gameId != null && (gameSession = gameService.getGameSession(gameId)) != null){
            List<Puzzle> puzzles = gameSession.getPuzzles();
            List<PuzzleDescription> positions = new ArrayList<>();
            for(Puzzle p: puzzles){
                positions.add(new PuzzleDescription(p.getX(), p.getY(), p.getPuzzleId(), p.getOriginalId(), p.getComponentId(), p.isInGame(), p.getNeighbors()));
            }
            PuzzlesState puzzlesState = new PuzzlesState();
            puzzlesState.setPuzzles(positions);
            puzzlesState.setState(PuzzlesState.State.FINE);
            List<String> users = gameSession.getUsers();
            puzzlesState.setUsers(users.size());
            puzzlesState.setSeconds(Instant.now().toEpochMilli() / 1000 - gameSession.getStart());
            puzzlesState.setWidth(gameSession.getWidth());
            puzzlesState.setHeight(gameSession.getHeight());
            puzzlesState.setGridHeight(gameSession.getGridHeight());
            puzzlesState.setFieldHeight(gameSession.getFieldHeight());
            log.info("Return current puzzle state: puzzles = {} puzzleState = {}", puzzles, puzzlesState);
            messageSender.sendMessage(sessionId, "state", puzzlesState);
        }else{
            PuzzlesState puzzlesState = new PuzzlesState();
            puzzlesState.setState(PuzzlesState.State.TERMINATED);
            log.info("Game session is not found for sessionId: {}", sessionId);
            messageSender.sendMessage(sessionId, "state", puzzlesState);
        }
    }

    @MessageMapping("/connects")
    public void connectPuzzles(@Header("simpSessionId") String sessionId, PuzzleConnectionRequest connect){
        String gameId = gameService.getGameIdBySessionId(sessionId);
        log.info("Connect: {}", connect);
        if(gameId != null){
            List<Puzzle> puzzles = gameService.connectPuzzles(connect.getPuzzle1Id(), connect.getPuzzle2Id(), gameId);
            log.info("Puzzles: {}", puzzles);
            PuzzleConnection puzzleConnection = new PuzzleConnection();
            puzzleConnection.setStatus(gameService.isGameSolved(gameId) ? PuzzleConnection.Status.SOLVED: PuzzleConnection.Status.NOT_SOLVED);
            messageSender.sendMessage(sessionId, "/connects", puzzleConnection);
            List<ConnectDesc> connects = new LinkedList<>();
            for(Puzzle p: puzzles){
                connects.add(new ConnectDesc(p.getPuzzleId(), p.getComponentId(), p.getX(), p.getY()));
            }
            puzzleConnection.setConnects(connects);
            List<String> users = gameService.getAllUsers(gameId);
            for(String user: users){
                if(user.equals(sessionId)) continue;
                log.info("Sending connects update to user {}. Updates: {}", user, puzzleConnection);
                messageSender.sendMessage(user, "/connects", puzzleConnection);
            }
        }else{
            PuzzlesState puzzlesState = new PuzzlesState();
            puzzlesState.setState(PuzzlesState.State.TERMINATED);
            log.info("Game session is not found for sessionId: {}", sessionId);
            messageSender.sendMessage(sessionId, "/connects", puzzlesState);
        }
    }
}
