package com.example.puzzlesbackend.dto.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuzzleConnection {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ConnectDesc> connects;
    private Status status;
    public enum Status{
        SOLVED, NOT_SOLVED
    }
}
