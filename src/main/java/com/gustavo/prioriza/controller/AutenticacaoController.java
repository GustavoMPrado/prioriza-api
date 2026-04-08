package com.gustavo.prioriza.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gustavo.prioriza.config.AppAuthProperties;
import com.gustavo.prioriza.dto.LoginRequest;
import com.gustavo.prioriza.dto.LoginResponse;
import com.gustavo.prioriza.exception.UnauthorizedException;
import com.gustavo.prioriza.security.JwtService;
import com.gustavo.prioriza.security.LoginRateLimiter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/autenticacao")
public class AutenticacaoController {

    private final AppAuthProperties authProps;
    private final JwtService jwtService;
    private final LoginRateLimiter loginRateLimiter;

    public AutenticacaoController(AppAuthProperties authProps, JwtService jwtService, LoginRateLimiter loginRateLimiter) {
        this.authProps = authProps;
        this.jwtService = jwtService;
        this.loginRateLimiter = loginRateLimiter;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> autenticar(@Valid @RequestBody LoginRequest requisicao, HttpServletRequest request) {
        String chaveCliente = resolverChaveCliente(request);

        if (!loginRateLimiter.allow(chaveCliente)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).build();
        }

        if (!authProps.getUsername().equals(requisicao.getUsername())
                || !authProps.getPassword().equals(requisicao.getPassword())) {
            throw new UnauthorizedException();
        }

        String token = jwtService.generateAccessToken(requisicao.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private String resolverChaveCliente(HttpServletRequest request) {
        String encaminhadoPor = request.getHeader("X-Forwarded-For");

        if (encaminhadoPor != null && !encaminhadoPor.isBlank()) {
            int virgula = encaminhadoPor.indexOf(',');
            return virgula > 0 ? encaminhadoPor.substring(0, virgula).trim() : encaminhadoPor.trim();
        }

        return request.getRemoteAddr();
    }
}



