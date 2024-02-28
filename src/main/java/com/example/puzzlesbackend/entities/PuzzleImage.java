package com.example.puzzlesbackend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Data;

@Data
@Entity
public class PuzzleImage {
    @Id
    private String id;

    @Lob
    private byte[] data;

    @Column(name = "original_name")
    String originalName;
}
