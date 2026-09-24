package com.andanza.backend.auth;

import com.andanza.backend.exception.TooManyRequestsException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// Frena la fuerza bruta contra el login: tras 5 intentos fallidos con el mismo correo desde la misma
// dirección, se bloquea por 15 minutos. Vive en memoria, así que vale para una sola instancia del backend
// y se reinicia con la app; para varias instancias haría falta guardarlo en la base o en un caché compartido.
@Component
public class LoginAttemptLimiter {

    static final int MAX_FAILURES = 5;
    static final Duration WINDOW = Duration.ofMinutes(15);
    private static final int CLEANUP_THRESHOLD = 10_000;

    private record Attempts(int failures, Instant firstFailure) {
    }

    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();
    private final Clock clock;

    public LoginAttemptLimiter() {
        this(Clock.systemUTC());
    }

    LoginAttemptLimiter(Clock clock) {
        this.clock = clock;
    }

    public void checkAllowed(String key) {
        Attempts current = attempts.get(key);
        if (current == null) {
            return;
        }
        if (expired(current)) {
            attempts.remove(key, current);
            return;
        }
        if (current.failures() >= MAX_FAILURES) {
            throw new TooManyRequestsException("email",
                    "Demasiados intentos fallidos. Intenta de nuevo en " + WINDOW.toMinutes() + " minutos.");
        }
    }

    public void recordFailure(String key) {
        Instant now = clock.instant();
        attempts.merge(key, new Attempts(1, now), (old, fresh) ->
                expired(old) ? fresh : new Attempts(old.failures() + 1, old.firstFailure()));
        if (attempts.size() > CLEANUP_THRESHOLD) {
            attempts.values().removeIf(this::expired);
        }
    }

    public void reset(String key) {
        attempts.remove(key);
    }

    private boolean expired(Attempts value) {
        return value.firstFailure().plus(WINDOW).isBefore(clock.instant());
    }
}
