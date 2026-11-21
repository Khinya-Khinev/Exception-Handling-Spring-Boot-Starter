package com.khinya.exceptionhandling.security.entrypoints;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class DefaultAuthenticationEntryPointTest {

    private final HandlerExceptionResolver resolver = mock(HandlerExceptionResolver.class);
    private final DefaultAuthenticationEntryPoint entryPoint = new DefaultAuthenticationEntryPoint(resolver);

    @Nested
    class Commence {

        @Test
        void commence_WhenInvoked_DelegatesExceptionToResolver() {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            AuthenticationException authException = new BadCredentialsException("Unauthorized access");

            entryPoint.commence(request, response, authException);

            verify(resolver).resolveException(request, response, null, authException);
        }
    }
}
