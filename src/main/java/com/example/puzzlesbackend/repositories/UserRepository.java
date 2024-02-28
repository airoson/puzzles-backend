package com.example.puzzlesbackend.repositories;

import com.example.puzzlesbackend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmailOrName(String email, String name);

    User findByName(String name);
}
