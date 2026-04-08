package com.gustavo.prioriza.exception;

public class AtividadeNaoEncontradaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AtividadeNaoEncontradaException(Long id) {
        super("Atividade não encontrada com id: " + id);
    }

    public AtividadeNaoEncontradaException(String message) {
        super(message);
    }
}
