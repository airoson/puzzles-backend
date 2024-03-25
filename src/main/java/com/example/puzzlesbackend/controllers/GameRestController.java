package com.example.puzzlesbackend.controllers;

import com.example.puzzlesbackend.dto.GameParams;
import com.example.puzzlesbackend.services.GameService;
import com.example.puzzlesbackend.services.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/game")
@Slf4j
public class GameRestController {
    @Autowired
    private GameService gameService;

    @PostMapping("/create")
    @CrossOrigin(origins = {"http://localhost:3000", "0.0.0.0:3000", "192.168.43.165:3000", "http://127.0.0.1:3000", "http://localhost", "http://localhost:3001"}, allowCredentials = "true")
    public ResponseEntity<?> createGame(@RequestParam MultipartFile file, @RequestParam(name = "puzzles_count") int puzzlesCount, Principal principal){
        try{
            BufferedImage image = ImageIO.read(file.getInputStream());
            GameParams gameParams = gameService.createGameSession(principal != null ? principal.getName() : "test", image, puzzlesCount);
            return ResponseEntity.ok().body(gameParams);
        }catch(IOException e){
            log.error(e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
