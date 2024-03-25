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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<PuzzleDescription> puzzles;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer users;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long seconds;
    private State state;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer width;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer height;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer gridHeight;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer fieldHeight;

    public enum State{
        FINE, TERMINATED, SOLVED
    }
}
