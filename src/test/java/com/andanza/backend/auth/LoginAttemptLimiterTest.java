package com.andanza.backend.auth;

import com.andanza.backend.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginAttemptLimiterTest {

    private static class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-09-24T12:00:00Z");

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }

    private final MutableClock clock = new MutableClock();
    private final LoginAttemptLimiter limiter = new LoginAttemptLimiter(clock);

    private void fail(String key, int times) {
        for (int i = 0; i < times; i++) {
            limiter.checkAllowed(key);
            limiter.recordFailure(key);
        }
    }

    @Test
    void allowsFiveFailuresAndBlocksTheNextAttempt() {
        fail("ana@example.com|1.1.1.1", 5);

        assertThatThrownBy(() -> limiter.checkAllowed("ana@example.com|1.1.1.1"))
                .isInstanceOf(TooManyRequestsException.class);
    }

    @Test
    void doesNotBlockBeforeTheFifthFailure() {
        fail("ana@example.com|1.1.1.1", 4);

        assertThatCode(() -> limiter.checkAllowed("ana@example.com|1.1.1.1")).doesNotThrowAnyException();
    }

    @Test
    void unlocksOnceTheWindowHasPassed() {
        fail("ana@example.com|1.1.1.1", 5);
        clock.advance(LoginAttemptLimiter.WINDOW.plusSeconds(1));

        assertThatCode(() -> limiter.checkAllowed("ana@example.com|1.1.1.1")).doesNotThrowAnyException();
    }

    @Test
    void aSuccessfulLoginClearsTheCounter() {
        fail("ana@example.com|1.1.1.1", 4);
        limiter.reset("ana@example.com|1.1.1.1");
        fail("ana@example.com|1.1.1.1", 4);

        assertThatCode(() -> limiter.checkAllowed("ana@example.com|1.1.1.1")).doesNotThrowAnyException();
    }

    @Test
    void eachEmailAndAddressIsCountedSeparately() {
        fail("ana@example.com|1.1.1.1", 5);

        assertThatCode(() -> limiter.checkAllowed("ana@example.com|2.2.2.2")).doesNotThrowAnyException();
        assertThatCode(() -> limiter.checkAllowed("luis@example.com|1.1.1.1")).doesNotThrowAnyException();
    }
}
