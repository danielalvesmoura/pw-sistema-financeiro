package com.ifpr.backend.security;

import com.ifpr.backend.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

class AuthRateLimitServiceTest {
    @Test void sixthLoginForSameIdentityIsBlocked() {
        var service = new AuthRateLimitService();
        for (int i = 0; i < 5; i++) service.checkLogin("127.0.0.1", "teste@example.com");
        assertThatThrownBy(() -> service.checkLogin("127.0.0.1", " TESTE@example.com "))
            .isInstanceOf(TooManyRequestsException.class);
    }
    @Test void successResetsIdentityCounter() {
        var service = new AuthRateLimitService();
        for (int i = 0; i < 5; i++) service.checkLogin("127.0.0.1", "teste@example.com");
        service.loginSucceeded("127.0.0.1", "teste@example.com");
        assertThatCode(() -> service.checkLogin("127.0.0.1", "teste@example.com")).doesNotThrowAnyException();
    }
    @Test void fourthRecoveryIsBlocked() {
        var service = new AuthRateLimitService();
        for (int i = 0; i < 3; i++) service.checkPasswordRecovery("127.0.0.1", "teste@example.com");
        assertThatThrownBy(() -> service.checkPasswordRecovery("127.0.0.1", "teste@example.com"))
            .isInstanceOf(TooManyRequestsException.class);
    }
}
