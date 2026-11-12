package com.ifpr.backend.security;

import com.ifpr.backend.exception.TooManyRequestsException;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class AuthRateLimitService {
    private static final Duration LOGIN_WINDOW = Duration.ofMinutes(15);
    private static final Duration RECOVERY_WINDOW = Duration.ofHours(1);

    private static final int LOGIN_IDENTITY_LIMIT = 5;
    private static final int LOGIN_IP_LIMIT = 20;
    private static final int RECOVERY_IDENTITY_LIMIT = 3;
    private static final int RECOVERY_IP_LIMIT = 10;

    private final ConcurrentHashMap<String, AttemptWindow> attempts = new ConcurrentHashMap<>();
    private final AtomicInteger operations = new AtomicInteger();

    public void checkLogin(String ipAddress, String email) {
        String ip = normalizeIp(ipAddress);
        String identityKey = "login:identity:" + ip + ":" + normalizeEmail(email);

        consume(identityKey, LOGIN_IDENTITY_LIMIT, LOGIN_WINDOW);
        consume("login:ip:" + ip, LOGIN_IP_LIMIT, LOGIN_WINDOW);
    }

    public void loginSucceeded(String ipAddress, String email) {
        String ip = normalizeIp(ipAddress);
        attempts.remove("login:identity:" + ip + ":" + normalizeEmail(email));
    }

    public void checkPasswordRecovery(String ipAddress, String email) {
        String ip = normalizeIp(ipAddress);
        String identityKey = "recovery:identity:" + ip + ":" + normalizeEmail(email);

        consume(identityKey, RECOVERY_IDENTITY_LIMIT, RECOVERY_WINDOW);
        consume("recovery:ip:" + ip, RECOVERY_IP_LIMIT, RECOVERY_WINDOW);
    }

    private void consume(String key, int limit, Duration windowDuration) {
        long now = System.currentTimeMillis();

        attempts.compute(key, (ignored, current) -> {
            if (current == null || current.expiresAtMs() <= now) {
                return new AttemptWindow(1, now + windowDuration.toMillis());
            }

            if (current.attempts() >= limit) {
                long retryAfterSeconds = Math.max(
                    1,
                    (current.expiresAtMs() - now + 999) / 1000
                );

                throw new TooManyRequestsException(
                    "Muitas tentativas. Aguarde antes de tentar novamente.",
                    retryAfterSeconds
                );
            }

            return new AttemptWindow(current.attempts() + 1, current.expiresAtMs());
        });

        cleanupExpiredEntries(now);
    }

    private void cleanupExpiredEntries(long now) {
        if (operations.incrementAndGet() % 200 != 0) {
            return;
        }

        attempts.entrySet().removeIf(entry -> entry.getValue().expiresAtMs() <= now);
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeIp(String ipAddress) {
        return ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress.trim();
    }

    private record AttemptWindow(int attempts, long expiresAtMs) {}
}
