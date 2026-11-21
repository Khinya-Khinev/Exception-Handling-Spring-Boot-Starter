package com.khinya.exceptionhandling.security;

import com.khinya.exceptionhandling.helpers.ProblemDetailResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DefaultSecurityExceptionHandler {

    @ExceptionHandler(AccessDeniedException.class)
    protected ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return ProblemDetailResponse
                .of(HttpStatus.FORBIDDEN,
                        "ACCESS_DENIED",
                        "Access denied")
                .build();
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ProblemDetail handleAuth(AuthenticationException ex) {
        return ProblemDetailResponse
                .of(HttpStatus.UNAUTHORIZED,
                        "AUTHENTICATION_FAILED",
                        "Authentication failed")
                .build();
    }
}