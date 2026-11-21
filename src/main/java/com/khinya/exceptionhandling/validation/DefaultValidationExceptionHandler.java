package com.khinya.exceptionhandling.validation;

import com.khinya.exceptionhandling.helpers.ProblemDetailResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class DefaultValidationExceptionHandler {

    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";
    private static final String VALIDATION_TITLE = "Validation failed";
    private static final String VALIDATION_DETAIL = "One or more fields failed validation";

    @ExceptionHandler(ConstraintViolationException.class)
    protected ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        return ProblemDetailResponse
                .of(HttpStatus.BAD_REQUEST,
                    VALIDATION_ERROR_CODE,
                    VALIDATION_TITLE)
                .detail(VALIDATION_DETAIL)
                .invalidParams(extractErrors(ex))
                .build();
    }

    private Map<String, String> extractErrors(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> {
                            String path = v.getPropertyPath().toString();
                            return path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
                        },
                        v -> v.getMessage() != null ? v.getMessage() : "Invalid value",
                        (existing, replacement) -> existing + " | " + replacement
                ));
    }
}