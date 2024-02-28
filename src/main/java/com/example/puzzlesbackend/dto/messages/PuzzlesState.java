package com.example.puzzlesbackend.dto.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PuzzlesState {

    private List<PuzzleDescription> puzzles;
    private State state;

    public enum State{
        FINE, TERMINATED, SOLVED
    }
}
