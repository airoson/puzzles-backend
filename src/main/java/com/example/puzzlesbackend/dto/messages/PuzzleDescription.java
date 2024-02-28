package com.example.puzzlesbackend.dto.messages;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PuzzleDescription {
    private Integer x;
    private Integer y;
    private Integer id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer componentId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean inGame;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Integer> neighbors;

    public PuzzleDescription(Integer x, Integer y, Integer id) {
        this.x = x;
        this.y = y;
        this.id = id;
    }
}
