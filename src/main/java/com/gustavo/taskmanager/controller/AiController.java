package com.gustavo.taskmanager.controller;

import org.springframework.web.bind.annotation.*;

import com.gustavo.taskmanager.dto.AiSuggestPriorityRequestDTO;
import com.gustavo.taskmanager.dto.AiSuggestPriorityResponseDTO;
import com.gustavo.taskmanager.service.AiService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/suggest-priority")
    public AiSuggestPriorityResponseDTO suggestPriority(@Valid @RequestBody AiSuggestPriorityRequestDTO dto) {
        return aiService.suggestPriority(dto);
    }
}
