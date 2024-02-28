package com.example.puzzlesbackend.controllers;

import com.example.puzzlesbackend.dto.LoginRequest;
import com.example.puzzlesbackend.dto.SigninResult;
import com.example.puzzlesbackend.dto.UserData;
import com.example.puzzlesbackend.entities.User;
import com.example.puzzlesbackend.services.UserService;
import com.example.puzzlesbackend.utils.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api")
@Slf4j
public class AuthenticationController {
    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private UserService userService;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;


    @PostMapping("/signin")
    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost", "http://localhost:3001"}, allowCredentials = "true")
    public ResponseEntity<?> createAccount(@RequestBody UserData userData){
        User user = new User();
        user.setEmail(userData.getEmail());
        user.setName(userData.getName());
        user.setPassword(encoder.encode(userData.getPassword()));
        boolean res = userService.createNewUser(user);
        if(res)
            return ResponseEntity.ok().body(new SigninResult(user.getId(), SigninResult.Status.SUCCESS, null));
        else
            return ResponseEntity.ok().body(new SigninResult(null, SigninResult.Status.FAULT, "Can't create user, because username or email is taken"));
    }

    @PostMapping("/signup")
    @CrossOrigin(origins = {"http://localhost:3000", "http://localhost", "http://localhost:3001"}, allowCredentials = "true")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response){
        Map<String, String> message = new TreeMap<>();
        try{
            log.info("Trying to authenticate user : {}", loginRequest);
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getName(), loginRequest.getPassword()));
        }catch(BadCredentialsException e){
            message.put("status", "Not authenticated");
            return ResponseEntity.status(401).body(message);
        }
        String token = jwtUtils.generateToken(loginRequest.getName());
        Cookie authCookie = new Cookie("auth", token);
        authCookie.setHttpOnly(true);
        response.addCookie(authCookie);
        message.put("status", "Authenticated");
        return ResponseEntity.ok().body(message);
    }
}
