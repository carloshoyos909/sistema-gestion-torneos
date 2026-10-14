package com.torneos.auth;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Bloqueo temporal tras varios intentos fallidos (protección contra fuerza bruta).
 * Es en memoria: si despliegas varias instancias, usa Redis o Bucket4j.
 */
@Service
public class LoginAttemptService {

    private static final int MAX_FAILS = 5;
    private static final Duration LOCK_TIME = Duration.ofMinutes(15);

    private record State(int fails, Instant lockedUntil) {}

    private final Map<String, State> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        State s = attempts.get(key);
        return s != null && s.lockedUntil() != null && s.lockedUntil().isAfter(Instant.now());
    }

    public void loginFailed(String key) {
        attempts.merge(key, new State(1, null), (old, ignored) -> {
            int fails = old.fails() + 1;
            return fails >= MAX_FAILS
                    ? new State(0, Instant.now().plus(LOCK_TIME))
                    : new State(fails, null);
        });
    }

    public void loginSucceeded(String key) {
        attempts.remove(key);
    }
}
