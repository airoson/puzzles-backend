package com.example.puzzlesbackend.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class Puzzle {
    private int puzzleId;
    private int x;
    private int y;
    private boolean inGame;
    private int componentId;
    // ll bb rr tp
    private int connectors;
    private List<Integer> neighbors;
}
