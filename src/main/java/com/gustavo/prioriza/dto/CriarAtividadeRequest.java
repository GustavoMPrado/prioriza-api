package com.gustavo.prioriza.dto;

import java.time.LocalDate;

import com.gustavo.prioriza.entity.PrioridadeAtividade;
import com.gustavo.prioriza.entity.StatusAtividade;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CriarAtividadeRequest {

    @NotBlank(message = "título é obrigatório")
    @Size(min = 3, max = 120, message = "título deve ter entre 3 e 120 caracteres")
    private String titulo;

    @Size(max = 500, message = "descrição deve ter no máximo 500 caracteres")
    private String descricao;

    private StatusAtividade status;
    private PrioridadeAtividade prioridade;

    @FutureOrPresent(message = "dataLimite não pode ser no passado")
    private LocalDate dataLimite;

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
}

