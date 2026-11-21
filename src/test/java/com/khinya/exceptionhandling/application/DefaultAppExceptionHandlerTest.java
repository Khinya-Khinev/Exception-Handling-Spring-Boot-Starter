package com.khinya.exceptionhandling.application;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DefaultAppExceptionHandlerTest {

    private final DefaultAppExceptionHandler handler = new DefaultAppExceptionHandler();

    private enum TestErrorCode implements ErrorCode {
        USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_NOT_FOUND"),
        DATABASE_FAILURE(HttpStatus.INTERNAL_SERVER_ERROR, "DATABASE_FAILURE");

        private final HttpStatus httpStatus;
        private final String code;

        TestErrorCode(HttpStatus httpStatus, String code) {
            this.httpStatus = httpStatus;
            this.code = code;
        }

        @Override
        public HttpStatus getHttpStatus() {
            return httpStatus;
        }

        @Override
        public String getCode() {
            return code;
        }
    }

    @Nested
    class HandleAppException {

        @Test
        void handleAppException_When4xxWithoutMessageSource_ReturnsProblemDetailWithCodeAsTitle() {
            AppException ex = AppException.of(TestErrorCode.USER_NOT_FOUND);

            ProblemDetail result = handler.handleAppException(ex);

            assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
            assertThat(result.getTitle()).isEqualTo("USER_NOT_FOUND");
            assertThat(result.getProperties()).containsEntry("error_code", "USER_NOT_FOUND");
        }

        @Test
        void handleAppException_When5xxWithoutMessageSource_LogsAndReturnsProblemDetailWith5xxStatus() {
            AppException ex = AppException.of(TestErrorCode.DATABASE_FAILURE);

            ProblemDetail result = handler.handleAppException(ex);

            assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
            assertThat(result.getTitle()).isEqualTo("DATABASE_FAILURE");
            assertThat(result.getProperties()).containsEntry("error_code", "DATABASE_FAILURE");
        }

        @Test
        void handleAppException_WithMessageSourceAndTranslation_ReturnsProblemDetailWithLocalizedTitle() {
            MessageSource messageSource = mock(MessageSource.class);
            Locale currentLocale = LocaleContextHolder.getLocale();
            when(messageSource.getMessage("USER_NOT_FOUND", null, "USER_NOT_FOUND", currentLocale))
                    .thenReturn("User does not exist");
            handler.setMessageSource(messageSource);
            AppException ex = AppException.of(TestErrorCode.USER_NOT_FOUND);

            ProblemDetail result = handler.handleAppException(ex);

            assertThat(result.getTitle()).isEqualTo("User does not exist");
            assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
            assertThat(result.getProperties()).containsEntry("error_code", "USER_NOT_FOUND");
        }

        @Test
        void handleAppException_WithErrorContext_PopulatesContextPropertiesInProblemDetail() {
            AppException ex = AppException.of(TestErrorCode.USER_NOT_FOUND)
                    .with("userId", 42L)
                    .with("traceId", "trace-abc");

            ProblemDetail result = handler.handleAppException(ex);

            assertThat(result.getProperties())
                    .containsEntry("userId", 42L)
                    .containsEntry("traceId", "trace-abc");
        }
    }

    @Nested
    class ResolveTitle {

        @Test
        void resolveTitle_WhenMessageSourceIsNull_ReturnsCode() {
            String title = handler.resolveTitle("ORDER_NOT_FOUND");

            assertThat(title).isEqualTo("ORDER_NOT_FOUND");
        }

        @Test
        void resolveTitle_WhenMessageSourceIsSet_ReturnsMessageFromSource() {
            MessageSource messageSource = mock(MessageSource.class);
            Locale locale = LocaleContextHolder.getLocale();
            when(messageSource.getMessage("ORDER_NOT_FOUND", null, "ORDER_NOT_FOUND", locale))
                    .thenReturn("Order not found in database");
            handler.setMessageSource(messageSource);

            String title = handler.resolveTitle("ORDER_NOT_FOUND");

            assertThat(title).isEqualTo("Order not found in database");
        }
    }
}
