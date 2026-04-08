package com.gustavo.prioriza;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.gustavo.prioriza.controller.AtividadeController;
import com.gustavo.prioriza.dto.AtividadeResponse;
import com.gustavo.prioriza.entity.Atividade;
import com.gustavo.prioriza.entity.PrioridadeAtividade;
import com.gustavo.prioriza.entity.StatusAtividade;
import com.gustavo.prioriza.exception.AtividadeNaoEncontradaException;
import com.gustavo.prioriza.exception.GlobalExceptionHandler;
import com.gustavo.prioriza.security.JwtAuthenticationFilter;
import com.gustavo.prioriza.service.AtividadeService;

@WebMvcTest(
        controllers = AtividadeController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import({ GlobalExceptionHandler.class, AtividadeControllerTest.MockConfig.class })
class AtividadeControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        AtividadeService atividadeService() {
            return Mockito.mock(AtividadeService.class);
        }
    }

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AtividadeService atividadeService;

    @Test
    void post_quandoValido_deveRetornar201ComBody() throws Exception {
        String body = """
        {
          "titulo": "Nova atividade",
          "descricao": "Teste",
          "status": "A_FAZER",
          "prioridade": "ALTA",
          "dataLimite": "2030-01-01"
        }
        """;

        Atividade criada = new Atividade();
        criada.setTitulo("Nova atividade");
        criada.setDescricao("Teste");
        criada.setStatus(StatusAtividade.A_FAZER);
        criada.setPrioridade(PrioridadeAtividade.ALTA);
        criada.setDataLimite(LocalDate.of(2030, 1, 1));

        when(atividadeService.criar(any())).thenReturn(criada);

        AtividadeResponse response = new AtividadeResponse();
        response.setId(1L);
        response.setTitulo(criada.getTitulo());
        response.setDescricao(criada.getDescricao());
        response.setStatus(criada.getStatus());
        response.setPrioridade(criada.getPrioridade());
        response.setDataLimite(criada.getDataLimite());
        response.setCriadoEm(LocalDateTime.now());
        response.setAtualizadoEm(LocalDateTime.now());

        when(atividadeService.paraResponse(criada)).thenReturn(response);

        mockMvc.perform(post("/atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.titulo").value("Nova atividade"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"))
                .andExpect(jsonPath("$.status").value("A_FAZER"))
                .andExpect(jsonPath("$.dataLimite").value("2030-01-01"));
    }

    @Test
    void post_quandoTituloVazio_deveRetornar400() throws Exception {
        String body = """
        {
          "titulo": "",
          "descricao": "x"
        }
        """;

        mockMvc.perform(post("/atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_quandoDescricaoMaiorQue500_deveRetornar400() throws Exception {
        String big = "a".repeat(501);

        String body = """
        {
          "titulo": "Teste",
          "descricao": "%s"
        }
        """.formatted(big);

        mockMvc.perform(post("/atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_quandoDataLimiteNoPassado_deveRetornar400() throws Exception {
        LocalDate ontem = LocalDate.now().minusDays(1);

        String body = """
        {
          "titulo": "Teste",
          "dataLimite": "%s"
        }
        """.formatted(ontem);

        mockMvc.perform(post("/atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void post_quandoEnumInvalido_deveRetornar400() throws Exception {
        String body = """
        {
          "titulo": "Teste",
          "status": "INVALIDO"
        }
        """;

        mockMvc.perform(post("/atividades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAtividades_deveRetornarPagina() throws Exception {
        AtividadeResponse dto = new AtividadeResponse();
        dto.setId(1L);
        dto.setTitulo("Primeira");
        dto.setDescricao("Desc");
        dto.setStatus(StatusAtividade.A_FAZER);
        dto.setPrioridade(PrioridadeAtividade.MEDIA);
        dto.setCriadoEm(LocalDateTime.now());
        dto.setAtualizadoEm(LocalDateTime.now());

        Page<AtividadeResponse> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10),
                1
        );

        when(atividadeService.buscar(nullable(String.class), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/atividades")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Primeira"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getById_quandoExiste_deveRetornar200() throws Exception {
        Atividade atividade = new Atividade();
        atividade.setTitulo("A1");
        atividade.setDescricao("D1");
        atividade.setStatus(StatusAtividade.EM_ANDAMENTO);
        atividade.setPrioridade(PrioridadeAtividade.ALTA);

        when(atividadeService.buscarOuFalhar(5L)).thenReturn(atividade);

        AtividadeResponse response = new AtividadeResponse();
        response.setId(5L);
        response.setTitulo(atividade.getTitulo());
        response.setDescricao(atividade.getDescricao());
        response.setStatus(atividade.getStatus());
        response.setPrioridade(atividade.getPrioridade());
        response.setDataLimite(atividade.getDataLimite());
        response.setCriadoEm(LocalDateTime.now());
        response.setAtualizadoEm(LocalDateTime.now());

        when(atividadeService.paraResponse(atividade)).thenReturn(response);

        mockMvc.perform(get("/atividades/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("EM_ANDAMENTO"))
                .andExpect(jsonPath("$.prioridade").value("ALTA"));
    }

    @Test
    void getById_quandoNaoExiste_deveRetornar404() throws Exception {
        when(atividadeService.buscarOuFalhar(999L)).thenThrow(new AtividadeNaoEncontradaException(999L));

        mockMvc.perform(get("/atividades/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void put_quandoValido_deveRetornar200() throws Exception {
        String body = """
        {
          "titulo": "Atualizado",
          "descricao": "Nova desc",
          "status": "CONCLUIDA",
          "prioridade": "BAIXA",
          "dataLimite": "2030-02-02"
        }
        """;

        AtividadeResponse response = new AtividadeResponse();
        response.setId(10L);
        response.setTitulo("Atualizado");
        response.setDescricao("Nova desc");
        response.setStatus(StatusAtividade.CONCLUIDA);
        response.setPrioridade(PrioridadeAtividade.BAIXA);
        response.setDataLimite(LocalDate.of(2030, 2, 2));
        response.setCriadoEm(LocalDateTime.now());
        response.setAtualizadoEm(LocalDateTime.now());

        when(atividadeService.atualizar(eq(10L), any())).thenReturn(response);

        mockMvc.perform(put("/atividades/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("CONCLUIDA"));
    }

    @Test
    void patch_quandoValido_deveRetornar200() throws Exception {
        String body = """
        {
          "titulo": "Parcial"
        }
        """;

        AtividadeResponse response = new AtividadeResponse();
        response.setId(11L);
        response.setTitulo("Parcial");
        response.setStatus(StatusAtividade.A_FAZER);
        response.setPrioridade(PrioridadeAtividade.MEDIA);
        response.setCriadoEm(LocalDateTime.now());
        response.setAtualizadoEm(LocalDateTime.now());

        when(atividadeService.atualizarParcialmente(eq(11L), any())).thenReturn(response);

        mockMvc.perform(patch("/atividades/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.titulo").value("Parcial"));
    }

    @Test
    void delete_deveRetornar204() throws Exception {
        doNothing().when(atividadeService).excluirPorId(20L);

        mockMvc.perform(delete("/atividades/20"))
                .andExpect(status().isNoContent());
    }
}
