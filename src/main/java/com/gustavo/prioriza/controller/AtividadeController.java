package com.gustavo.prioriza.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.gustavo.prioriza.dto.AtividadeResponse;
import com.gustavo.prioriza.dto.AtualizarAtividadeParcialRequest;
import com.gustavo.prioriza.dto.AtualizarAtividadeRequest;
import com.gustavo.prioriza.dto.CriarAtividadeRequest;
import com.gustavo.prioriza.entity.Atividade;
import com.gustavo.prioriza.entity.PrioridadeAtividade;
import com.gustavo.prioriza.entity.StatusAtividade;
import com.gustavo.prioriza.service.AtividadeService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/atividades")
public class AtividadeController {

    private static final int PAGINA_PADRAO = 0;
    private static final int TAMANHO_PADRAO = 10;
    private static final int TAMANHO_MAXIMO = 50;

    private final AtividadeService atividadeService;

    public AtividadeController(AtividadeService atividadeService) {
        this.atividadeService = atividadeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtividadeResponse criarAtividade(@Valid @RequestBody CriarAtividadeRequest dto) {
        Atividade atividadeCriada = atividadeService.criar(dto);
        return atividadeService.paraResponse(atividadeCriada);
    }

    @GetMapping
    public Page<AtividadeResponse> buscarAtividades(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) StatusAtividade status,
            @RequestParam(required = false) PrioridadeAtividade prioridade,
            @RequestParam(defaultValue = "" + PAGINA_PADRAO) int page,
            @RequestParam(defaultValue = "" + TAMANHO_PADRAO) int size
    ) {
        int paginaSegura = Math.max(page, 0);
        int tamanhoSeguro = Math.min(Math.max(size, 1), TAMANHO_MAXIMO);
        Pageable pageable = PageRequest.of(paginaSegura, tamanhoSeguro);
        return atividadeService.buscar(q, status, prioridade, pageable);
    }

    @GetMapping("/{id}")
    public AtividadeResponse buscarAtividadePorId(@PathVariable Long id) {
        Atividade atividade = atividadeService.buscarOuFalhar(id);
        return atividadeService.paraResponse(atividade);
    }

    @PutMapping("/{id}")
    public AtividadeResponse atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarAtividadeRequest dto) {
        return atividadeService.atualizar(id, dto);
    }

    @PatchMapping("/{id}")
    public AtividadeResponse atualizarParcialmente(@PathVariable Long id, @Valid @RequestBody AtualizarAtividadeParcialRequest dto) {
        return atividadeService.atualizarParcialmente(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluirAtividade(@PathVariable Long id) {
        atividadeService.excluirPorId(id);
    }
}





