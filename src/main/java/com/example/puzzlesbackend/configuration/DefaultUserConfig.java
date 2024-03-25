package com.example.puzzlesbackend.configuration;

import com.example.puzzlesbackend.entities.User;
import com.example.puzzlesbackend.services.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DefaultUserConfig {
    @Value("${game.default-user-password}")
    private String password;
    @Value("${game.default-user-login}")
    private String login;
    @Value("${game.default-user-email}")
    private String email;

    @Bean
    public CommandLineRunner commandLineRunner(UserService userService, PasswordEncoder encoder){
        return (args) -> {
            userService.createNewUser(new User(
               login, email, encoder.encode(password)
            ));
        };
    }
}
