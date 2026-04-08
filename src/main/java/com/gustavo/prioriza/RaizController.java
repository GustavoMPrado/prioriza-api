package com.gustavo.prioriza;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RaizController {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public String raiz() {
        return "{\"status\":\"ok\",\"service\":\"prioriza\"}";
    }
}