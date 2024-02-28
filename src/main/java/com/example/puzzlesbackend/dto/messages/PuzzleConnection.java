package com.example.puzzlesbackend.dto.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PuzzleConnection {
    private List<ConnectDesc> connects;
    private Status status;
    public enum Status{
        SOLVED, NOT_SOLVED
    }
}
