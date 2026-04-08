package com.gustavo.prioriza.security;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String cabecalhoAutorizacao = request.getHeader("Authorization");

        if (StringUtils.hasText(cabecalhoAutorizacao) && cabecalhoAutorizacao.startsWith("Bearer ")) {
            String tokenJwt = cabecalhoAutorizacao.substring(7).trim();
            String usuarioAutenticado = jwtService.getSubjectIfValid(tokenJwt);

            if (usuarioAutenticado != null) {
                org.springframework.security.core.context.SecurityContextHolder.getContext()
                        .setAuthentication(new JwtUserAuthentication(usuarioAutenticado));
            }
        }

        filterChain.doFilter(request, response);
    }
}
