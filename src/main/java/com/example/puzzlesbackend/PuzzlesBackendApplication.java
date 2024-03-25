package com.example.puzzlesbackend;

import com.example.puzzlesbackend.services.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class PuzzlesBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PuzzlesBackendApplication.class, args);
    }
}
