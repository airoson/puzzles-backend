package com.example.puzzlesbackend.dto.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuzzleConnectionRequest {
    private int puzzle1Id;
    private int puzzle2Id;
}
