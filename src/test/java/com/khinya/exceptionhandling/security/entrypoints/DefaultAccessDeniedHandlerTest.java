package com.khinya.exceptionhandling.security.entrypoints;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultAccessDeniedHandlerTest {

    private final HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
    private final DefaultAccessDeniedHandler accessDeniedHandler = new DefaultAccessDeniedHandler(resolver);

    @Nested
    class Handle {

        @Test
        void handle_WhenInvoked_DelegatesExceptionToResolver() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AccessDeniedException accessDeniedException = new AccessDeniedException("Access is denied");

            accessDeniedHandler.handle(request, response, accessDeniedException);

            verify(resolver).resolveException(request, response, null, accessDeniedException);
        }
    }
}
