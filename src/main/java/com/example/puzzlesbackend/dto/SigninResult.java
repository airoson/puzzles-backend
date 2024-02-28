package com.example.puzzlesbackend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SigninResult {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long id;
    private Status status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message;

    public enum Status{
        SUCCESS, FAULT
    }
}
