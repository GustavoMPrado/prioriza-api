package com.gustavo.prioriza.controller;

import java.time.Instant;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SaudeController {

    @GetMapping("/saude")
    public Map<String, Object> saude() {
        return Map.of(
            "status", "UP",
            "momento", Instant.now().toString()
        );
    }
}