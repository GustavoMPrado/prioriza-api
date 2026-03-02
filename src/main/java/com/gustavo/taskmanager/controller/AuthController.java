package com.gustavo.taskmanager.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gustavo.taskmanager.config.AppAuthProperties;
import com.gustavo.taskmanager.dto.LoginRequest;
import com.gustavo.taskmanager.dto.LoginResponse;
import com.gustavo.taskmanager.exception.UnauthorizedException;
import com.gustavo.taskmanager.security.JwtService;
import com.gustavo.taskmanager.security.LoginRateLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AppAuthProperties authProps;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;

    public AuthController(AppAuthProperties authProps, JwtService jwtService, LoginRateLimiter loginRateLimiter) {
        this.authProps = authProps;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest dto, HttpServletRequest request) {
        String key = clientKey(request);

        if (!loginRateLimiter.allow(key)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        if (!authProps.getUsername().equals(dto.getUsername()) || !authProps.getPassword().equals(dto.getPassword())) {
            throw new UnauthorizedException();
        }

        String token = jwtService.generateToken(dto.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private String clientKey(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            int comma = forwarded.indexOf(',');
            return comma > 0 ? forwarded.substring(0, comma).trim() : forwarded.trim();
        }
        return request.getRemoteAddr();
    }
}



