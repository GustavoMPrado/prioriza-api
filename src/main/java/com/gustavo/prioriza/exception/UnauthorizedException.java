package com.gustavo.prioriza.exception;

public class UnauthorizedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UnauthorizedException() {
        super("Credenciais inválidas");
    }
}
