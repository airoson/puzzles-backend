package com.example.puzzlesbackend.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTasks {
    private GameService gameService;

    @Scheduled(fixedRate = 600000, initialDelay = 600000)
    public void deleteOldGames(){

    }
}
