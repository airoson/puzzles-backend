package com.example.puzzlesbackend.dto.messages;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Move {
    private int id;
    private int x;
    private int y;
}
