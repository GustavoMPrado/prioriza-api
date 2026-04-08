package com.gustavo.prioriza.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.gustavo.prioriza.entity.PrioridadeAtividade;
import com.gustavo.prioriza.entity.StatusAtividade;

public class AtividadeResponse {

    private Long id;
    private String titulo;
    private String descricao;
    private StatusAtividade status;
    private PrioridadeAtividade prioridade;
    private LocalDate dataLimite;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public AtividadeResponse() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public StatusAtividade getStatus() {
        return status;
    }

    public void setStatus(StatusAtividade status) {
        this.status = status;
    }

    public PrioridadeAtividade getPrioridade() {
        return prioridade;
    }

    public void setPrioridade(PrioridadeAtividade prioridade) {
        this.prioridade = prioridade;
    }

    public LocalDate getDataLimite() {
        return dataLimite;
    }

    public void setDataLimite(LocalDate dataLimite) {
        this.dataLimite = dataLimite;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}