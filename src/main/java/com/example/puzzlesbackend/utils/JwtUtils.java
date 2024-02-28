package com.example.puzzlesbackend.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class JwtUtils {
    private String secret;
    private String issuer;
    private Algorithm algorithm;
    private JWTVerifier tokenVerifier;
    private long tokenExpirationTimeMillis;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.issuer}") String issuer,
                    @Value("${jwt.expiration-time-minutes}") int expirationTimeMinutes){
        algorithm = Algorithm.HMAC256(secret);
        tokenExpirationTimeMillis = 1000 * 60 * expirationTimeMinutes;
        tokenVerifier = JWT.require(algorithm).withIssuer(issuer).build();
        this.issuer = issuer;
        this.secret = secret;
    }

    public String generateToken(String subject){
        return JWT.create()
                .withSubject(subject)
                .withIssuedAt(Instant.now())
                .withIssuer(issuer)
                .withExpiresAt(Instant.now().plusMillis(tokenExpirationTimeMillis))
                .sign(algorithm);
    }

    public String validateToken(String token){
        try{
            DecodedJWT decodedJWT = tokenVerifier.verify(token);
            return decodedJWT.getSubject();
        }catch(Exception e){
            log.error("Can't verify token {}", token);
        }
        return null;
    }
}
