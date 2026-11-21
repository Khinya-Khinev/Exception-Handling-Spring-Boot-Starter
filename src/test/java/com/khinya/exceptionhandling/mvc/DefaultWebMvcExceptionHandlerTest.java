package com.khinya.exceptionhandling.mvc;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultWebMvcExceptionHandlerTest {

    private final DefaultWebMvcExceptionHandler handler = new DefaultWebMvcExceptionHandler();

    @SuppressWarnings("unused")
    private void dummyTargetMethod(String param) { /* test method */ }

    private MethodParameter createMethodParameter() throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod("dummyTargetMethod", String.class);
        return new MethodParameter(method, 0);
    }

    @Nested
    class HandleMethodArgumentNotValid {

        @Test
        void handleMethodArgumentNotValid_WithSingleFieldError_ReturnsBadRequestWithInvalidParams() throws Exception {
            MethodParameter parameter = createMethodParameter();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "email", "must be a valid email"));
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
            HttpHeaders headers = new HttpHeaders();
            WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

            ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex, headers, HttpStatus.BAD_REQUEST, request);

            assertThat(response).isNotNull();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isInstanceOf(ProblemDetail.class);

            ProblemDetail problemDetail = (ProblemDetail) response.getBody();
            assertThat(problemDetail.getTitle()).isEqualTo("Validation failed");
            assertThat(problemDetail.getDetail()).isEqualTo("One or more fields failed validation");
            assertThat(problemDetail.getProperties()).containsEntry("error_code", "VALIDATION_ERROR");

            @SuppressWarnings("unchecked")
            Map<String, String> invalidParams = (Map<String, String>) problemDetail.getProperties().get("invalid_params");
            assertThat(invalidParams).containsEntry("email", "must be a valid email");
        }

        @Test
        void handleMethodArgumentNotValid_WithNullDefaultMessage_UsesDefaultInvalidValue() throws Exception {
            MethodParameter parameter = createMethodParameter();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "username", null));
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
            HttpHeaders headers = new HttpHeaders();
            WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

            ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex, headers, HttpStatus.BAD_REQUEST, request);

            assertThat(response).isNotNull();
            ProblemDetail problemDetail = (ProblemDetail) response.getBody();
            assertThat(problemDetail).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, String> invalidParams = (Map<String, String>) problemDetail.getProperties().get("invalid_params");
            assertThat(invalidParams).containsEntry("username", "Invalid value");
        }

        @Test
        void handleMethodArgumentNotValid_WithMultipleErrorsForSameField_MergesMessagesWithPipe() throws Exception {
            MethodParameter parameter = createMethodParameter();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "password", "must be at least 8 characters"));
            bindingResult.addError(new FieldError("target", "password", "must contain at least one digit"));
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
            HttpHeaders headers = new HttpHeaders();
            WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

            ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex, headers, HttpStatus.BAD_REQUEST, request);

            assertThat(response).isNotNull();
            ProblemDetail problemDetail = (ProblemDetail) response.getBody();
            assertThat(problemDetail).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, String> invalidParams = (Map<String, String>) problemDetail.getProperties().get("invalid_params");
            assertThat(invalidParams).containsEntry("password", "must be at least 8 characters | must contain at least one digit");
        }

        @Test
        void handleMethodArgumentNotValid_WithMultipleDistinctFields_ContainsAllFieldErrors() throws Exception {
            MethodParameter parameter = createMethodParameter();
            BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
            bindingResult.addError(new FieldError("target", "email", "must not be blank"));
            bindingResult.addError(new FieldError("target", "age", "must be 18 or older"));
            MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
            HttpHeaders headers = new HttpHeaders();
            WebRequest request = new ServletWebRequest(new MockHttpServletRequest());

            ResponseEntity<Object> response = handler.handleMethodArgumentNotValid(ex, headers, HttpStatus.BAD_REQUEST, request);

            assertThat(response).isNotNull();
            ProblemDetail problemDetail = (ProblemDetail) response.getBody();
            assertThat(problemDetail).isNotNull();

            @SuppressWarnings("unchecked")
            Map<String, String> invalidParams = (Map<String, String>) problemDetail.getProperties().get("invalid_params");
            assertThat(invalidParams)
                    .containsEntry("email", "must not be blank")
                    .containsEntry("age", "must be 18 or older");
        }
    }
}
