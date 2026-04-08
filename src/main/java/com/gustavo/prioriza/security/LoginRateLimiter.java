package com.gustavo.prioriza.security;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_MILLIS = 60_000L;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public boolean allow(String key) {
        long now = Instant.now().toEpochMilli();

        Window w = windows.compute(key, (k, current) -> {
            if (current == null || now - current.windowStart >= WINDOW_MILLIS) {
                return new Window(now, 1);
            }
            return new Window(current.windowStart, current.attempts + 1);
        });

        return w.attempts <= MAX_ATTEMPTS;
    }

    private record Window(long windowStart, int attempts) {}
}
