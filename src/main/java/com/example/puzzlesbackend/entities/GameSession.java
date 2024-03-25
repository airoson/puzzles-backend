package com.example.puzzlesbackend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.util.List;

@RedisHash("GameSession")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameSession {
    @Id
    @Indexed
    private String gameId;
    private String creator;
    private int width;
    private int height;
    private int puzzleSize;
    private int fieldHeight;
    private int gridHeight;
    private int components;
    private long start;
    private List<Puzzle> puzzles;
    private List<String> users;
}
