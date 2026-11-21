package com.khinya.exceptionhandling.security;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultSecurityExceptionHandlerTest {

    private final DefaultSecurityExceptionHandler handler = new DefaultSecurityExceptionHandler();

    @Nested
    class HandleAccessDenied {

        @Test
        void handleAccessDenied_WhenAccessDeniedException_ReturnsForbiddenProblemDetail() {
            AccessDeniedException ex = new AccessDeniedException("Forbidden action");

            ProblemDetail result = handler.handleAccessDenied(ex);

            assertThat(result.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
            assertThat(result.getTitle()).isEqualTo("Access denied");
            assertThat(result.getProperties()).containsEntry("error_code", "ACCESS_DENIED");
        }
    }

    @Nested
    class HandleAuth {

        @Test
        void handleAuth_WhenAuthenticationException_ReturnsUnauthorizedProblemDetail() {
            AuthenticationException ex = new BadCredentialsException("Invalid credentials");

            ProblemDetail result = handler.handleAuth(ex);

            assertThat(result.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
            assertThat(result.getTitle()).isEqualTo("Authentication failed");
            assertThat(result.getProperties()).containsEntry("error_code", "AUTHENTICATION_FAILED");
        }
    }
}
