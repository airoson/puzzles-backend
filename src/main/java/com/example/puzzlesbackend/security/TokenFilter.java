package com.example.puzzlesbackend.security;

import com.example.puzzlesbackend.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class TokenFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(request.getCookies() == null){
            filterChain.doFilter(request, response);
            log.info("Can't authenticate user because no cookie is present");
            return;
        }
        Cookie authCookie = Arrays.stream(request.getCookies()).filter(c -> c.getName().equals("auth")).findFirst().orElse(null);
        if(authCookie != null){
            String subject = jwtUtils.validateToken(authCookie.getValue());
            if(subject != null){
                log.info("User {} is authenticated", subject);
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                        subject,
                        null,
                        List.of()
                ));
            }
        }
        filterChain.doFilter(request, response);
    }
}
