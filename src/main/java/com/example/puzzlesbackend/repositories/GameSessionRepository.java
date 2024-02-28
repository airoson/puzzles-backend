package com.example.puzzlesbackend.repositories;

import com.example.puzzlesbackend.entities.GameSession;
import org.springframework.data.jpa.repository.JpaRepository;


public interface GameSessionRepository extends JpaRepository<GameSession, String> {

}
