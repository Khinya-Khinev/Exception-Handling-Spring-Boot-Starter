package com.khinya.exceptionhandling.mvc;

import com.khinya.exceptionhandling.helpers.ProblemDetailFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class DefaultWebMvcExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String VALIDATION_ERROR_CODE = "VALIDATION_ERROR";
    private static final String VALIDATION_TITLE = "Validation failed";
    private static final String VALIDATION_DETAIL = "One or more fields failed validation";

    protected ProblemDetailFactory factory;

    @Autowired
    public final void setProblemDetailFactory(ProblemDetailFactory factory) {
        this.factory = factory;
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            @NonNull MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request
    ) {
        log.warn("Handling method argument not valid exception: {}. Errors: {}",
                ex.getMessage(), ex.getBindingResult().getFieldErrors(), ex);

        ProblemDetail problemDetail = factory.problem(status, VALIDATION_ERROR_CODE, VALIDATION_TITLE)
                .detail(VALIDATION_DETAIL)
                .invalidParams(extractErrors(ex))
                .build();

        return createResponseEntity(problemDetail, headers, status, request);
    }

    private Map<String, String> extractErrors(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        e -> e.getDefaultMessage() != null ? e.getDefaultMessage() : "Invalid value",
                        this::mergeMessages
                ));
    }

    private String mergeMessages(String existing, String replacement) {
        return existing + " | " + replacement;
    }
}