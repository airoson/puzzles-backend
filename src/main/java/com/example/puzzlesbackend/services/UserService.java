package com.example.puzzlesbackend.services;

import com.example.puzzlesbackend.entities.User;
import com.example.puzzlesbackend.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public boolean createNewUser(User user){
        if(userRepository.existsByEmailOrName(user.getEmail(), user.getName())){
            return false;
        }
        userRepository.save(user);
        return true;
    }

    public User findUserByName(String name){
        return userRepository.findByName(name);
    }
}
