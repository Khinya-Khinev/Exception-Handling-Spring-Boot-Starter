package com.khinya.exceptionhandling.security;

import com.khinya.exceptionhandling.helpers.ProblemDetailFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class DefaultSecurityExceptionHandler {

    protected ProblemDetailFactory factory;

    @Autowired
    public final void setProblemDetailFactory(ProblemDetailFactory factory) {
        this.factory = factory;
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        return factory
                .problem(HttpStatus.FORBIDDEN,
                        "ACCESS_DENIED",
                        "Access denied")
                .build();
    }

    @ExceptionHandler(AuthenticationException.class)
    protected ProblemDetail handleAuth(AuthenticationException ex) {
        return factory
                .problem(HttpStatus.UNAUTHORIZED,
                        "AUTHENTICATION_FAILED",
                        "Authentication failed")
                .build();
    }
}