package com.example.puzzlesbackend.dto.messages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectDesc{
    private int id;
    private int componentId;
    private int x;
    private int y;
}
