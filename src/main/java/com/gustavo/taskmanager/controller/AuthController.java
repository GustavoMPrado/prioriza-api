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
    public ResponseEntity<LoginResponse> authenticate(@Valid @RequestBody LoginRequest requestBody, HttpServletRequest request) {
        String clientKey = resolveClientKey(request);

        if (!loginRateLimiter.allow(clientKey)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        if (!authProps.getUsername().equals(requestBody.getUsername()) || !authProps.getPassword().equals(requestBody.getPassword())) {
            throw new UnauthorizedException();
        }

        String token = jwtService.generateToken(requestBody.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            int comma = forwardedFor.indexOf(',');
            return comma > 0 ? forwardedFor.substring(0, comma).trim() : forwardedFor.trim();
        }
        return request.getRemoteAddr();
    }
}



