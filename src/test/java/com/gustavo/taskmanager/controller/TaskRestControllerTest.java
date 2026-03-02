package com.gustavo.taskmanager.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustavo.taskmanager.dto.TaskResponse;
import com.gustavo.taskmanager.entity.Task;
import com.gustavo.taskmanager.entity.TaskPriority;
import com.gustavo.taskmanager.entity.TaskStatus;
import com.gustavo.taskmanager.exception.GlobalExceptionHandler;
import com.gustavo.taskmanager.exception.TaskNotFoundException;
import com.gustavo.taskmanager.security.JwtAuthenticationFilter;
import com.gustavo.taskmanager.service.TaskService;

@WebMvcTest(
        controllers = TaskRestController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
@Import({ GlobalExceptionHandler.class, TaskRestControllerTest.MockConfig.class })
class TaskRestControllerTest {

    @TestConfiguration
    static class MockConfig {
        @Bean
        TaskService taskService() {
            return Mockito.mock(TaskService.class);
        }
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Autowired TaskService taskService;

    @Test
    void post_quandoValido_deveRetornar201ComBody() throws Exception {
        String body = """
        {
          "title": "Nova task",
          "description": "Teste",
          "status": "TODO",
          "priority": "HIGH",
          "dueDate": "2030-01-01"
        }
        """;

        Task created = new Task();
        created.setTitle("Nova task");
        created.setDescription("Teste");
        created.setStatus(TaskStatus.TODO);
        created.setPriority(TaskPriority.HIGH);
        created.setDueDate(LocalDate.of(2030, 1, 1));
        created.prePersist();

        when(taskService.create(any())).thenReturn(created);

        TaskResponse resp = new TaskResponse();
        resp.setId(1L);
        resp.setTitle(created.getTitle());
        resp.setDescription(created.getDescription());
        resp.setStatus(created.getStatus());
        resp.setPriority(created.getPriority());
        resp.setDueDate(created.getDueDate());
        resp.setCreatedAt(created.getCreatedAt());
        resp.setUpdatedAt(created.getUpdatedAt());

        when(taskService.toTaskResponse(created)).thenReturn(resp);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Nova task"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.dueDate").value("2030-01-01"));
    }

    @Test
    void post_quandoTitleVazio_deveRetornar400ComApiError() throws Exception {
        String body = """
        {
          "title": "",
          "description": "x"
        }
        """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.path").value("/tasks"))
                .andExpect(jsonPath("$.errors.title").exists());
    }

    @Test
    void post_quandoDescriptionMaiorQue500_deveRetornar400ComErroDescription() throws Exception {
        String big = "a".repeat(501);

        String body = """
        {
          "title": "Teste",
          "description": "%s"
        }
        """.formatted(big);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors.description").exists());
    }

    @Test
    void post_quandoDueDateNoPassado_deveRetornar400ComErroDueDate() throws Exception {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        String body = """
        {
          "title": "Teste",
          "dueDate": "%s"
        }
        """.formatted(yesterday);

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Erro de validação"))
                .andExpect(jsonPath("$.errors.dueDate").exists());
    }

    @Test
    void post_quandoEnumInvalido_deveRetornar400ComMensagemJsonInvalido() throws Exception {
        String body = """
        {
          "title": "Teste",
          "status": "INVALIDO"
        }
        """;

        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("JSON inválido ou enum inválido (status/priority)."))
                .andExpect(jsonPath("$.path").value("/tasks"));
    }

    @Test
    void getTasks_deveRetornarPagina() throws Exception {
        TaskResponse dto = new TaskResponse();
        dto.setId(1L);
        dto.setTitle("Primeira");
        dto.setDescription("Desc");
        dto.setStatus(TaskStatus.TODO);
        dto.setPriority(TaskPriority.MEDIUM);
        dto.setCreatedAt(LocalDateTime.now());
        dto.setUpdatedAt(LocalDateTime.now());

        Page<TaskResponse> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 10),
                1
        );

        when(taskService.search(nullable(String.class), any(), any(), any())).thenReturn(page);

        mockMvc.perform(get("/tasks")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Primeira"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    void getById_quandoExiste_deveRetornar200() throws Exception {
        Task task = new Task();
        task.setTitle("T1");
        task.setDescription("D1");
        task.setStatus(TaskStatus.DOING);
        task.setPriority(TaskPriority.HIGH);
        task.prePersist();

        when(taskService.getTaskOrThrow(5L)).thenReturn(task);

        TaskResponse resp = new TaskResponse();
        resp.setId(5L);
        resp.setTitle(task.getTitle());
        resp.setDescription(task.getDescription());
        resp.setStatus(task.getStatus());
        resp.setPriority(task.getPriority());
        resp.setDueDate(task.getDueDate());
        resp.setCreatedAt(task.getCreatedAt());
        resp.setUpdatedAt(task.getUpdatedAt());

        when(taskService.toTaskResponse(task)).thenReturn(resp);

        mockMvc.perform(get("/tasks/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.status").value("DOING"))
                .andExpect(jsonPath("$.priority").value("HIGH"));
    }

    @Test
    void getById_quandoNaoExiste_deveRetornar404ComApiError() throws Exception {
        when(taskService.getTaskOrThrow(999L)).thenThrow(new TaskNotFoundException(999L));

        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task not found with id: 999"))
                .andExpect(jsonPath("$.path").value("/tasks/999"));
    }

    @Test
    void put_quandoValido_deveRetornar200() throws Exception {
        String body = """
        {
          "title": "Atualizado",
          "description": "Nova desc",
          "status": "DONE",
          "priority": "LOW",
          "dueDate": "2030-02-02"
        }
        """;

        TaskResponse resp = new TaskResponse();
        resp.setId(10L);
        resp.setTitle("Atualizado");
        resp.setDescription("Nova desc");
        resp.setStatus(TaskStatus.DONE);
        resp.setPriority(TaskPriority.LOW);
        resp.setDueDate(LocalDate.of(2030, 2, 2));
        resp.setCreatedAt(LocalDateTime.now());
        resp.setUpdatedAt(LocalDateTime.now());

        when(taskService.update(eq(10L), any())).thenReturn(resp);

        mockMvc.perform(put("/tasks/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void patch_quandoValido_deveRetornar200() throws Exception {
        String body = """
        {
          "title": "Parcial"
        }
        """;

        TaskResponse resp = new TaskResponse();
        resp.setId(11L);
        resp.setTitle("Parcial");
        resp.setStatus(TaskStatus.TODO);
        resp.setPriority(TaskPriority.MEDIUM);
        resp.setCreatedAt(LocalDateTime.now());
        resp.setUpdatedAt(LocalDateTime.now());

        when(taskService.patch(eq(11L), any())).thenReturn(resp);

        mockMvc.perform(patch("/tasks/11")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$.title").value("Parcial"));
    }

    @Test
    void delete_deveRetornar204() throws Exception {
        doNothing().when(taskService).deleteById(20L);

        mockMvc.perform(delete("/tasks/20"))
                .andExpect(status().isNoContent());
    }
}



