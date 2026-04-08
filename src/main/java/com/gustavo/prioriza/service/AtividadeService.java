package com.gustavo.prioriza.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gustavo.prioriza.dto.AtividadeResponse;
import com.gustavo.prioriza.dto.AtualizarAtividadeParcialRequest;
import com.gustavo.prioriza.dto.AtualizarAtividadeRequest;
import com.gustavo.prioriza.dto.CriarAtividadeRequest;
import com.gustavo.prioriza.entity.Atividade;
import com.gustavo.prioriza.entity.PrioridadeAtividade;
import com.gustavo.prioriza.entity.StatusAtividade;
import com.gustavo.prioriza.exception.AtividadeNaoEncontradaException;
import com.gustavo.prioriza.repository.AtividadeRepository;

@Service
public class AtividadeService {

    private final AtividadeRepository atividadeRepository;

    public AtividadeService(AtividadeRepository atividadeRepository) {
        this.atividadeRepository = atividadeRepository;
    }

    public Atividade criar(CriarAtividadeRequest dto) {
        Atividade atividade = new Atividade();
        atividade.setTitulo(dto.getTitulo());
        atividade.setDescricao(dto.getDescricao());

        if (dto.getStatus() != null) {
            atividade.setStatus(dto.getStatus());
        }

        if (dto.getPrioridade() != null) {
            atividade.setPrioridade(dto.getPrioridade());
        } else {
            atividade.setPrioridade(PrioridadeAtividade.MEDIA);
        }

        atividade.setDataLimite(dto.getDataLimite());

        return atividadeRepository.save(atividade);
    }

    public List<Atividade> buscarTodas() {
        return atividadeRepository.findAll();
    }

    public Page<AtividadeResponse> listar(Pageable pageable) {
        return atividadeRepository.findAll(pageable).map(this::paraResponse);
    }

    public Page<AtividadeResponse> buscar(String q, StatusAtividade status, PrioridadeAtividade prioridade, Pageable pageable) {
        String busca = q == null ? null : q.trim();
        boolean temBusca = busca != null && !busca.isBlank();

        if (!temBusca && status == null && prioridade == null) {
            return listar(pageable);
        }

        if (temBusca) {
            String like = "%" + busca.toLowerCase() + "%";
            return atividadeRepository.buscar(like, status, prioridade, pageable).map(this::paraResponse);
        }

        return atividadeRepository.filtrarApenas(status, prioridade, pageable).map(this::paraResponse);
    }

    public Atividade buscarOuFalhar(Long id) {
        return atividadeRepository.findById(id)
                .orElseThrow(() -> new AtividadeNaoEncontradaException(id));
    }

    public AtividadeResponse paraResponse(Atividade atividade) {
        AtividadeResponse response = new AtividadeResponse();
        response.setId(atividade.getId());
        response.setTitulo(atividade.getTitulo());
        response.setDescricao(atividade.getDescricao());
        response.setStatus(atividade.getStatus());
        response.setPrioridade(atividade.getPrioridade());
        response.setDataLimite(atividade.getDataLimite());
        response.setCriadoEm(atividade.getCriadoEm());
        response.setAtualizadoEm(atividade.getAtualizadoEm());
        return response;
    }

    public AtividadeResponse atualizar(Long id, AtualizarAtividadeRequest dto) {
        Atividade atividade = buscarOuFalhar(id);

        atividade.setTitulo(dto.getTitulo());
        atividade.setDescricao(dto.getDescricao());

        if (dto.getStatus() != null) {
            atividade.setStatus(dto.getStatus());
        }

        if (dto.getPrioridade() != null) {
            atividade.setPrioridade(dto.getPrioridade());
        } else {
            atividade.setPrioridade(PrioridadeAtividade.MEDIA);
        }

        atividade.setDataLimite(dto.getDataLimite());

        Atividade atividadeSalva = atividadeRepository.save(atividade);
        return paraResponse(atividadeSalva);
    }

    public AtividadeResponse atualizarParcialmente(Long id, AtualizarAtividadeParcialRequest dto) {
        Atividade atividade = buscarOuFalhar(id);

        if (dto.getTitulo() != null) {
            atividade.setTitulo(dto.getTitulo());
        }
        if (dto.getDescricao() != null) {
            atividade.setDescricao(dto.getDescricao());
        }
        if (dto.getStatus() != null) {
            atividade.setStatus(dto.getStatus());
        }
        if (dto.getPrioridade() != null) {
            atividade.setPrioridade(dto.getPrioridade());
        }
        if (dto.getDataLimite() != null) {
            atividade.setDataLimite(dto.getDataLimite());
        }

        Atividade atividadeSalva = atividadeRepository.save(atividade);
        return paraResponse(atividadeSalva);
    }

    public void excluirPorId(Long id) {
        Atividade atividade = buscarOuFalhar(id);
        atividadeRepository.delete(atividade);
    }
}
