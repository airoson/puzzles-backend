package com.example.puzzlesbackend.services;

import com.example.puzzlesbackend.dto.GameParams;
import com.example.puzzlesbackend.dto.ImageCropParams;
import com.example.puzzlesbackend.entities.GameSession;
import com.example.puzzlesbackend.entities.Puzzle;
import com.example.puzzlesbackend.repositories.GameSessionRepository;
import com.example.puzzlesbackend.utils.PuzzlesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class GameService {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private GameSessionRepository repository;
    @Autowired
    private ImageService imageService;
    @Autowired
    private PuzzlesService puzzlesService;

    @Value("${game.puzzle-size-default}")
    private int puzzleDefaultSize;

    public String getGameIdBySessionId(String sessionId){
        return (String) redisTemplate.opsForHash().get("GAMES", sessionId);
    }

    public GameParams createGameSession(String creator, BufferedImage image, int puzzlesCount){
        String gameId = UUID.randomUUID().toString();
        ImageCropParams params = imageService.getCropPuzzleParams(puzzlesCount, image.getWidth(), image.getHeight());
        List<Puzzle> layout = puzzlesService.generateLayout(params.width(), params.height());
        List<BufferedImage> puzzles = imageService.cutImage(image, params, layout);
        int id = 0;
        for(var puzzle: puzzles){
            try{
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(puzzle, "png", out);
                imageService.addImage(out.toByteArray(), null, gameId + "-" + id++);
            }catch(IOException e){
                log.info(e.getMessage());
                return null;
            }
        }
        GameSession gameSession = new GameSession();
        gameSession.setGameId(gameId);
        gameSession.setCreator(creator);
        gameSession.setWidth(params.width());
        gameSession.setHeight(params.height());
        gameSession.setPuzzles(layout);
        gameSession.setUsers(List.of());
        gameSession.setComponents(puzzlesCount);
        repository.save(gameSession);
        return new GameParams(gameId, params.width(), params.height());
    }

    public void addUser(String gameId, String user){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        if(gameSession != null){
            var users = gameSession.getUsers();
            if(users == null){
                users = new LinkedList<>();
            }
            users.add(user);
            gameSession.setUsers(users);
            repository.save(gameSession);
            redisTemplate.opsForHash().put("GAMES", user, gameId);
            log.info("Adding user {} to game {}", user, gameId);
        }
    }

    public void addSessionToGameId(String gameId, String sessionId){
        redisTemplate.opsForHash().put("GAMES", sessionId, gameId);
    }

    public List<String> getAllUsers(String gameId){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        if(gameSession != null){
            return new ArrayList<>(gameSession.getUsers());
        }
        return List.of();
    }

    public void removeUser(String gameId, String user){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        if(gameSession != null){
            gameSession.getUsers().remove(user);
            repository.save(gameSession);
        }
    }

    public List<Puzzle> updatePosition(int puzzleId, int x, int y, String gameId){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        if(gameSession != null){
            Puzzle puzzle = gameSession.getPuzzles().get(puzzleId);
            int movementX = x - puzzle.getX();
            int movementY = y - puzzle.getY();
            int component = puzzle.getComponentId();
            puzzle.setX(x);
            puzzle.setY(y);
            if(!puzzle.isInGame()) puzzle.setInGame(true);
            List<Puzzle> result = new LinkedList<>();
            for(Puzzle p: gameSession.getPuzzles()){
                if(p.getComponentId() == component && p.getPuzzleId() != puzzleId){
                    p.setX(p.getX() + movementX);
                    p.setY(p.getY() + movementY);
                    result.add(p);
                }
            }
            repository.save(gameSession);
            return result;
        }
        return List.of();
    }

    public List<Puzzle> connectPuzzles(int puzzle1Id, int puzzle2Id, int x, int y, String gameId){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        if(gameSession != null){
            List<Puzzle> result = new LinkedList<>();
            Puzzle puzzle1 = gameSession.getPuzzles().get(puzzle1Id);
            Puzzle puzzle2 = gameSession.getPuzzles().get(puzzle2Id);
            if(puzzle1 == null || puzzle2 == null) return List.of();
            int component1Id = puzzle1.getComponentId();
            int component2Id = puzzle2.getComponentId();
            int movementX = x - puzzle2.getX();
            int movementY = y - puzzle2.getY();
            puzzle2.setX(x);
            puzzle2.setY(y);
            puzzle2.setComponentId(component1Id);
            log.info("Connect puzzles {} {} : {} {}", puzzle1Id, puzzle2Id, component1Id, component2Id);
            for(Puzzle puzzle: gameSession.getPuzzles()){
                if(puzzle.getComponentId() == component2Id && puzzle.getPuzzleId() != puzzle2Id){
                    puzzle.setComponentId(component1Id);
                    puzzle.setX(puzzle.getX() + movementX);
                    puzzle.setY(puzzle.getY() + movementY);
                    result.add(puzzle);
                }
            }
            gameSession.setComponents(gameSession.getComponents() - 1);
            log.info("Subtract from game components: {}", gameSession.getComponents());
            repository.save(gameSession);
            return result;
        }else return List.of();
    }

    public List<Puzzle> getAllPuzzles(String gameId){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        if(gameSession != null){
            return new ArrayList<>(gameSession.getPuzzles());
        }
        return List.of();
    }

    public String getCreator(String gameId){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        if(gameSession != null){
            return gameSession.getCreator();
        }
        return null;
    }

    public boolean isGameSolved(String gameId){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        return gameSession != null && gameSession.getComponents() <= 1;
    }

    public ImageCropParams getGameFieldSize(String gameId){
        GameSession gameSession = repository.findById(gameId).orElse(null);
        if(gameSession != null){
            return new ImageCropParams(gameSession.getWidth(), gameSession.getHeight(), 0);
        }
        return null;
    }

    public GameSession getGameSession(String gameId){
        return repository.findById(gameId).orElse(null);
    }
}
