package com.gustavo.taskmanager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.gustavo.taskmanager.dto.CreateTaskRequest;
import com.gustavo.taskmanager.dto.TaskPatchDTO;
import com.gustavo.taskmanager.dto.TaskResponse;
import com.gustavo.taskmanager.dto.UpdateTaskRequest;
import com.gustavo.taskmanager.entity.Task;
import com.gustavo.taskmanager.entity.TaskPriority;
import com.gustavo.taskmanager.entity.TaskStatus;
import com.gustavo.taskmanager.exception.TaskNotFoundException;
import com.gustavo.taskmanager.repository.TaskRepository;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        taskService = new TaskService(taskRepository);
    }

    @Test
    void create_quandoDtoSemStatusEPriority_deveManterStatusDefaultETrocarPriorityParaMEDIUM() {
        // Arrange
        CreateTaskRequest dto = new CreateTaskRequest();
        dto.setTitle("Tarefa 1");
        dto.setDescription("Desc");
        dto.setStatus(null);   // não seta -> Task já default TODO na entidade
        dto.setPriority(null); // service seta MEDIUM explicitamente
        dto.setDueDate(LocalDate.now().plusDays(1));

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            // simula JPA
            t.prePersist();
            return t;
        });

        // Act
        Task saved = taskService.create(dto);

        // Assert (no retorno)
        assertNotNull(saved);
        assertEquals("Tarefa 1", saved.getTitle());
        assertEquals("Desc", saved.getDescription());
        assertEquals(TaskStatus.TODO, saved.getStatus());         // default da entity
        assertEquals(TaskPriority.MEDIUM, saved.getPriority());   // setado pelo service
        assertEquals(dto.getDueDate(), saved.getDueDate());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        // Assert (o que foi mandado pro repo)
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).save(captor.capture());
        Task toSave = captor.getValue();
        assertEquals(TaskStatus.TODO, toSave.getStatus());
        assertEquals(TaskPriority.MEDIUM, toSave.getPriority());
    }

    @Test
    void create_quandoDtoComStatusEPriority_deveSalvarComEles() {
        // Arrange
        CreateTaskRequest dto = new CreateTaskRequest();
        dto.setTitle("Tarefa 2");
        dto.setDescription("Desc 2");
        dto.setStatus(TaskStatus.DOING);
        dto.setPriority(TaskPriority.HIGH);

        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.prePersist();
            return t;
        });

        // Act
        Task saved = taskService.create(dto);

        // Assert
        assertEquals(TaskStatus.DOING, saved.getStatus());
        assertEquals(TaskPriority.HIGH, saved.getPriority());
    }

    @Test
    void findById_quandoNaoExiste_deveLancarTaskNotFoundException() {
        when(taskRepository.findById(123L)).thenReturn(java.util.Optional.empty());

        TaskNotFoundException ex = assertThrows(TaskNotFoundException.class, () -> taskService.findById(123L));
        assertTrue(ex.getMessage().contains("123"));
    }

    @Test
    void update_quandoDtoSemPriority_deveForcarMEDIUM() {
        // Arrange
        Task existing = new Task();
        existing.setTitle("Antes");
        existing.setDescription("Antes desc");
        existing.setStatus(TaskStatus.TODO);
        existing.setPriority(TaskPriority.LOW);
        existing.prePersist();

        when(taskRepository.findById(10L)).thenReturn(java.util.Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.preUpdate(); // simula update no JPA
            return t;
        });

        UpdateTaskRequest dto = new UpdateTaskRequest();
        dto.setTitle("Depois");
        dto.setDescription("Depois desc");
        dto.setStatus(TaskStatus.DONE);
        dto.setPriority(null); // update -> service seta MEDIUM
        dto.setDueDate(LocalDate.now().plusDays(10));

        // Act
        TaskResponse updated = taskService.update(10L, dto);

        // Assert
        assertEquals("Depois", updated.getTitle());
        assertEquals("Depois desc", updated.getDescription());
        assertEquals(TaskStatus.DONE, updated.getStatus());
        assertEquals(TaskPriority.MEDIUM, updated.getPriority()); // regra do service
        assertEquals(dto.getDueDate(), updated.getDueDate());
        assertNotNull(updated.getCreatedAt());
        assertNotNull(updated.getUpdatedAt());
    }

    @Test
    void patch_quandoSoVemTitle_deveAtualizarApenasTitle() {
        // Arrange
        Task existing = new Task();
        existing.setTitle("Old");
        existing.setDescription("Desc");
        existing.setStatus(TaskStatus.TODO);
        existing.setPriority(TaskPriority.HIGH);
        existing.setDueDate(LocalDate.now().plusDays(5));
        existing.prePersist();

        when(taskRepository.findById(7L)).thenReturn(java.util.Optional.of(existing));
        when(taskRepository.save(any(Task.class))).thenAnswer(inv -> {
            Task t = inv.getArgument(0);
            t.preUpdate();
            return t;
        });

        TaskPatchDTO dto = new TaskPatchDTO();
        dto.setTitle("New");
        dto.setDescription(null);
        dto.setStatus(null);
        dto.setPriority(null);
        dto.setDueDate(null);

        // Act
        TaskResponse patched = taskService.patch(7L, dto);

        // Assert
        assertEquals("New", patched.getTitle());
        assertEquals("Desc", patched.getDescription());
        assertEquals(TaskStatus.TODO, patched.getStatus());
        assertEquals(TaskPriority.HIGH, patched.getPriority());
        assertEquals(existing.getDueDate(), patched.getDueDate());
    }

    @Test
    void delete_quandoExiste_deveChamarDeleteDoRepository() {
        Task existing = new Task();
        existing.setTitle("X");

        when(taskRepository.findById(5L)).thenReturn(java.util.Optional.of(existing));

        taskService.delete(5L);

        verify(taskRepository).delete(existing);
    }

    @Test
    void toResponseDTO_deveMapearCamposPrincipais() {
        Task task = new Task();
        task.setTitle("A");
        task.setDescription("B");
        task.setStatus(TaskStatus.DOING);
        task.setPriority(TaskPriority.LOW);
        task.setDueDate(LocalDate.now().plusDays(1));
        task.prePersist();

        TaskResponse dto = taskService.toResponseDTO(task);

        assertEquals("A", dto.getTitle());
        assertEquals("B", dto.getDescription());
        assertEquals(TaskStatus.DOING, dto.getStatus());
        assertEquals(TaskPriority.LOW, dto.getPriority());
        assertEquals(task.getDueDate(), dto.getDueDate());
        assertNotNull(dto.getCreatedAt());
        assertNotNull(dto.getUpdatedAt());
    }
}
