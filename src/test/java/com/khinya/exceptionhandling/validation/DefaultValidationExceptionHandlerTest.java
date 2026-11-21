package com.khinya.exceptionhandling.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultValidationExceptionHandlerTest {

    private final DefaultValidationExceptionHandler handler = new DefaultValidationExceptionHandler();

    @SuppressWarnings("unchecked")
    private ConstraintViolation<Object> createViolation(String propertyPath, String message) {
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn(propertyPath);
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn(message);
        return violation;
    }

    @Nested
    class HandleConstraintViolation {

        @Test
        void handleConstraintViolation_WithSimplePropertyPath_ReturnsBadRequestWithInvalidParams() {
            ConstraintViolation<Object> violation = createViolation("email", "must not be blank");
            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

            ProblemDetail result = handler.handleConstraintViolation(ex);

            assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
            assertThat(result.getTitle()).isEqualTo("Validation failed");
            assertThat(result.getDetail()).isEqualTo("One or more fields failed validation");
            assertThat(result.getProperties()).containsEntry("error_code", "VALIDATION_ERROR");

            @SuppressWarnings("unchecked")
            Map<String, String> invalidParams = (Map<String, String>) result.getProperties().get("invalid_params");
            assertThat(invalidParams).containsEntry("email", "must not be blank");
        }

        @Test
        void handleConstraintViolation_WithNestedPropertyPath_ExtractsLeafPropertyName() {
            ConstraintViolation<Object> violation = createViolation("createUser.request.email", "must be a valid email");
            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

            ProblemDetail result = handler.handleConstraintViolation(ex);

            @SuppressWarnings("unchecked")
            Map<String, String> invalidParams = (Map<String, String>) result.getProperties().get("invalid_params");
            assertThat(invalidParams)
                    .containsEntry("email", "must be a valid email")
                    .doesNotContainKey("createUser.request.email");
        }

        @Test
        void handleConstraintViolation_WithNullMessage_UsesDefaultInvalidValue() {
            ConstraintViolation<Object> violation = createViolation("username", null);
            ConstraintViolationException ex = new ConstraintViolationException(Set.of(violation));

            ProblemDetail result = handler.handleConstraintViolation(ex);

            @SuppressWarnings("unchecked")
            Map<String, String> invalidParams = (Map<String, String>) result.getProperties().get("invalid_params");
            assertThat(invalidParams).containsEntry("username", "Invalid value");
        }

        @Test
        void handleConstraintViolation_WithDuplicateFieldViolations_MergesMessagesWithPipe() {
            ConstraintViolation<Object> violation1 = createViolation("age", "must be positive");
            ConstraintViolation<Object> violation2 = createViolation("age", "must be at least 18");
            Set<ConstraintViolation<Object>> violations = new LinkedHashSet<>();
            violations.add(violation1);
            violations.add(violation2);
            ConstraintViolationException ex = new ConstraintViolationException(violations);

            ProblemDetail result = handler.handleConstraintViolation(ex);

            @SuppressWarnings("unchecked")
            Map<String, String> invalidParams = (Map<String, String>) result.getProperties().get("invalid_params");
            assertThat(invalidParams).hasEntrySatisfying("age", message ->
                    assertThat(message).contains("must be positive").contains("must be at least 18").contains(" | "));
        }
    }
}
