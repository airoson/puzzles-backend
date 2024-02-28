package com.example.puzzlesbackend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import java.util.List;

@RedisHash("GameSession")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameSession {
    @Id
    private String gameId;
    private String creator;
    private int width;
    private int height;
    private int components;
    private List<Puzzle> puzzles;
    private List<String> users;
}
