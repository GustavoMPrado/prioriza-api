package com.gustavo.taskmanager.service;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Component
public class OpenAiClient {

    private final RestClient restClient;
    private final String apiKey;
    private final String model;

    public OpenAiClient(
            RestClient.Builder builder,
            @Value("${app.ai.openai.api-key:}") String apiKey,
            @Value("${app.ai.openai.model:gpt-4.1-mini}") String model
    ) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = model;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(10));
        factory.setReadTimeout(Duration.ofSeconds(20));

        this.restClient = builder
                .baseUrl("https://api.openai.com/v1")
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(factory)
                .build();
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    public String suggestPriorityReasonText(String title, String description) {
        String prompt = buildPrompt(title, description);

        Map<String, Object> body = Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "user",
                                "content", List.of(Map.of("type", "input_text", "text", prompt))
                        )
                )
        );

        Map<String, Object> resp = restClient
                .post()
                .uri("/responses")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (resp == null) {
            return "";
        }

        Object output = resp.get("output");
        if (!(output instanceof List<?> outList) || outList.isEmpty()) {
            return "";
        }

        for (Object item : outList) {
            if (!(item instanceof Map<?, ?> m)) continue;

            Object content = m.get("content");
            if (!(content instanceof List<?> contentList)) continue;

            for (Object c : contentList) {
                if (!(c instanceof Map<?, ?> cm)) continue;
                Object type = cm.get("type");
                if (type != null && type.toString().contains("text")) {
                    Object text = cm.get("text");
                    if (text != null) return text.toString();
                }
            }
        }

        return "";
    }

    private String buildPrompt(String title, String description) {
        String safeTitle = title == null ? "" : title.trim();
        String safeDesc = description == null ? "" : description.trim();

        return ""
                + "You are a helpful assistant for a Task Manager app.\n"
                + "Return ONLY a compact JSON object with exactly these keys:\n"
                + "{ \"priority\": \"LOW|MEDIUM|HIGH\", \"reason\": \"...\" }\n"
                + "No markdown, no extra keys.\n\n"
                + "Task:\n"
                + "title: " + safeTitle + "\n"
                + "description: " + safeDesc + "\n";
    }
}


