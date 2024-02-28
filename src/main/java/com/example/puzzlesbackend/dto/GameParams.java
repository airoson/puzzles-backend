package com.example.puzzlesbackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GameParams {
    private String gameId;
    private int width;
    private int height;
}
