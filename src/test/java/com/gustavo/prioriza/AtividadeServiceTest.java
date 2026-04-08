package com.gustavo.prioriza;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.gustavo.prioriza.dto.AtividadeResponse;
import com.gustavo.prioriza.dto.AtualizarAtividadeParcialRequest;
import com.gustavo.prioriza.dto.AtualizarAtividadeRequest;
import com.gustavo.prioriza.dto.CriarAtividadeRequest;
import com.gustavo.prioriza.entity.Atividade;
import com.gustavo.prioriza.entity.PrioridadeAtividade;
import com.gustavo.prioriza.entity.StatusAtividade;
import com.gustavo.prioriza.exception.AtividadeNaoEncontradaException;
import com.gustavo.prioriza.repository.AtividadeRepository;
import com.gustavo.prioriza.service.AtividadeService;

class AtividadeServiceTest {

    private AtividadeRepository atividadeRepository;
    private AtividadeService atividadeService;

    @BeforeEach
    void setUp() {
        atividadeRepository = mock(AtividadeRepository.class);
        atividadeService = new AtividadeService(atividadeRepository);
    }

    @Test
    void criar_quandoDtoSemStatusEPrioridade_deveDefinirPrioridadeMedia() {
        CriarAtividadeRequest dto = new CriarAtividadeRequest();
        dto.setTitulo("Atividade 1");
        dto.setDescricao("Desc");
        dto.setStatus(null);
        dto.setPrioridade(null);
        dto.setDataLimite(LocalDate.now().plusDays(1));

        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(inv -> inv.getArgument(0));

        Atividade salva = atividadeService.criar(dto);

        assertNotNull(salva);
        assertEquals("Atividade 1", salva.getTitulo());
        assertEquals("Desc", salva.getDescricao());
        assertEquals(PrioridadeAtividade.MEDIA, salva.getPrioridade());
        assertEquals(dto.getDataLimite(), salva.getDataLimite());

        ArgumentCaptor<Atividade> captor = ArgumentCaptor.forClass(Atividade.class);
        verify(atividadeRepository).save(captor.capture());
        Atividade enviada = captor.getValue();
        assertEquals(PrioridadeAtividade.MEDIA, enviada.getPrioridade());
    }

    @Test
    void criar_quandoDtoComStatusEPrioridade_deveSalvarComEles() {
        CriarAtividadeRequest dto = new CriarAtividadeRequest();
        dto.setTitulo("Atividade 2");
        dto.setDescricao("Desc 2");
        dto.setStatus(StatusAtividade.A_FAZER);
        dto.setPrioridade(PrioridadeAtividade.ALTA);

        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(inv -> inv.getArgument(0));

        Atividade salva = atividadeService.criar(dto);

        assertEquals(StatusAtividade.A_FAZER, salva.getStatus());
        assertEquals(PrioridadeAtividade.ALTA, salva.getPrioridade());
    }

    @Test
    void buscarOuFalhar_quandoNaoExiste_deveLancarExcecao() {
        when(atividadeRepository.findById(123L)).thenReturn(Optional.empty());

        AtividadeNaoEncontradaException ex = assertThrows(
                AtividadeNaoEncontradaException.class,
                () -> atividadeService.buscarOuFalhar(123L)
        );

        assertTrue(ex.getMessage().contains("123"));
    }

    @Test
    void atualizar_quandoDtoSemPrioridade_deveForcarMedia() {
        Atividade existente = new Atividade();
        existente.setTitulo("Antes");
        existente.setDescricao("Antes desc");
        existente.setStatus(StatusAtividade.A_FAZER);
        existente.setPrioridade(PrioridadeAtividade.BAIXA);

        when(atividadeRepository.findById(10L)).thenReturn(Optional.of(existente));
        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarAtividadeRequest dto = new AtualizarAtividadeRequest();
        dto.setTitulo("Depois");
        dto.setDescricao("Depois desc");
        dto.setStatus(StatusAtividade.CONCLUIDA);
        dto.setPrioridade(null);
        dto.setDataLimite(LocalDate.now().plusDays(10));

        AtividadeResponse atualizada = atividadeService.atualizar(10L, dto);

        assertEquals("Depois", atualizada.getTitulo());
        assertEquals("Depois desc", atualizada.getDescricao());
        assertEquals(StatusAtividade.CONCLUIDA, atualizada.getStatus());
        assertEquals(PrioridadeAtividade.MEDIA, atualizada.getPrioridade());
        assertEquals(dto.getDataLimite(), atualizada.getDataLimite());
    }

    @Test
    void atualizarParcialmente_quandoSoVemTitulo_deveAtualizarApenasTitulo() {
        Atividade existente = new Atividade();
        existente.setTitulo("Old");
        existente.setDescricao("Desc");
        existente.setStatus(StatusAtividade.A_FAZER);
        existente.setPrioridade(PrioridadeAtividade.ALTA);
        existente.setDataLimite(LocalDate.now().plusDays(5));

        when(atividadeRepository.findById(7L)).thenReturn(Optional.of(existente));
        when(atividadeRepository.save(any(Atividade.class))).thenAnswer(inv -> inv.getArgument(0));

        AtualizarAtividadeParcialRequest dto = new AtualizarAtividadeParcialRequest();
        dto.setTitulo("New");
        dto.setDescricao(null);
        dto.setStatus(null);
        dto.setPrioridade(null);
        dto.setDataLimite(null);

        AtividadeResponse atualizada = atividadeService.atualizarParcialmente(7L, dto);

        assertEquals("New", atualizada.getTitulo());
        assertEquals("Desc", atualizada.getDescricao());
        assertEquals(StatusAtividade.A_FAZER, atualizada.getStatus());
        assertEquals(PrioridadeAtividade.ALTA, atualizada.getPrioridade());
        assertEquals(existente.getDataLimite(), atualizada.getDataLimite());
    }

    @Test
    void excluirPorId_quandoExiste_deveChamarDeleteDoRepository() {
        Atividade existente = new Atividade();
        existente.setTitulo("X");

        when(atividadeRepository.findById(5L)).thenReturn(Optional.of(existente));

        atividadeService.excluirPorId(5L);

        verify(atividadeRepository).delete(existente);
    }

    @Test
    void paraResponse_deveMapearCamposPrincipais() {
        Atividade atividade = new Atividade();
        atividade.setTitulo("A");
        atividade.setDescricao("B");
        atividade.setStatus(StatusAtividade.A_FAZER);
        atividade.setPrioridade(PrioridadeAtividade.BAIXA);
        atividade.setDataLimite(LocalDate.now().plusDays(1));

        AtividadeResponse dto = atividadeService.paraResponse(atividade);

        assertEquals("A", dto.getTitulo());
        assertEquals("B", dto.getDescricao());
        assertEquals(StatusAtividade.A_FAZER, dto.getStatus());
        assertEquals(PrioridadeAtividade.BAIXA, dto.getPrioridade());
        assertEquals(atividade.getDataLimite(), dto.getDataLimite());
    }
}