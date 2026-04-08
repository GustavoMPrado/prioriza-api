package com.gustavo.prioriza.security;

import java.util.Collections;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtUserAuthentication extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 1L;

    private final String username;

    public JwtUserAuthentication(String username) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        this.username = username;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return "";
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public String getName() {
        return username;
    }
}

