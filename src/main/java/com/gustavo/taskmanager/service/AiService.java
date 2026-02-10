package com.gustavo.taskmanager.service;

import java.util.Locale;

import org.springframework.stereotype.Service;

import com.gustavo.taskmanager.dto.AiSuggestPriorityRequestDTO;
import com.gustavo.taskmanager.dto.AiSuggestPriorityResponseDTO;
import com.gustavo.taskmanager.entity.TaskPriority;

@Service
public class AiService {

    private final OpenAiClient openAiClient;

    public AiService(OpenAiClient openAiClient) {
        this.openAiClient = openAiClient;
    }

    public AiSuggestPriorityResponseDTO suggestPriority(AiSuggestPriorityRequestDTO dto) {
        String title = dto.getTitle();
        String description = dto.getDescription();

        if (!openAiClient.isEnabled()) {
            return mock(title, description);
        }

        try {
            String text = openAiClient.suggestPriorityReasonText(title, description);
            Parsed parsed = Parsed.tryParseJson(text);

            if (parsed != null && parsed.priority != null && parsed.reason != null && !parsed.reason.isBlank()) {
                return new AiSuggestPriorityResponseDTO(parsed.priority, parsed.reason.trim());
            }

            return mock(title, description);
        } catch (Exception ex) {
            return mock(title, description);
        }
    }

    private AiSuggestPriorityResponseDTO mock(String title, String description) {
        String t = title == null ? "" : title.toLowerCase(Locale.ROOT);
        String d = description == null ? "" : description.toLowerCase(Locale.ROOT);

        TaskPriority p;
        String reason;

        if (t.contains("hoje") || t.contains("urgent") || t.contains("urgente") || d.contains("hoje") || d.contains("urgent") || d.contains("urgente")) {
            p = TaskPriority.HIGH;
            reason = "Parece ter urgência/curto prazo; sugiro HIGH para priorizar.";
        } else if (t.contains("pagar") || t.contains("bill") || d.contains("pagar") || d.contains("boleto") || d.contains("invoice")) {
            p = TaskPriority.MEDIUM;
            reason = "Tarefa financeira/contas costuma ser sensível a prazo; sugiro MEDIUM.";
        } else {
            p = TaskPriority.LOW;
            reason = "Sem sinais de urgência; sugiro LOW (ajuste se houver prazo curto).";
        }

        return new AiSuggestPriorityResponseDTO(p, reason);
    }

    private static class Parsed {
        private final TaskPriority priority;
        private final String reason;

        private Parsed(TaskPriority priority, String reason) {
            this.priority = priority;
            this.reason = reason;
        }

        static Parsed tryParseJson(String text) {
            if (text == null) return null;
            String s = text.trim();
            if (!s.startsWith("{") || !s.endsWith("}")) return null;

            String pr = extractStringValue(s, "priority");
            String rs = extractStringValue(s, "reason");

            if (pr == null || rs == null) return null;

            TaskPriority p = null;
            try {
                p = TaskPriority.valueOf(pr.trim().toUpperCase(Locale.ROOT));
            } catch (Exception ignored) {
            }
            if (p == null) return null;

            return new Parsed(p, rs);
        }

        private static String extractStringValue(String json, String key) {
            String k = "\"" + key + "\"";
            int ki = json.indexOf(k);
            if (ki < 0) return null;

            int colon = json.indexOf(':', ki + k.length());
            if (colon < 0) return null;

            int firstQuote = json.indexOf('"', colon + 1);
            if (firstQuote < 0) return null;

            int i = firstQuote + 1;
            StringBuilder sb = new StringBuilder();
            boolean escape = false;

            while (i < json.length()) {
                char c = json.charAt(i);
                if (escape) {
                    sb.append(c);
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    return sb.toString();
                } else {
                    sb.append(c);
                }
                i++;
            }
            return null;
        }
    }
}
