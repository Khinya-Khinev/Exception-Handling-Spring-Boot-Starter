package com.khinya.exceptionhandling.helpers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProblemDetailResponseTest {

    @Nested
    class Of {

        @Test
        void of_WithStatusAndErrorCodeAndTitle_CreatesBuilderWithCorrectProblemDetail() {
            ProblemDetailResponse.ProblemBuilder builder = ProblemDetailResponse.of(
                    HttpStatus.BAD_REQUEST, "BAD_REQUEST", "Bad Request");

            ProblemDetail result = builder.build();

            assertThat(result.getStatus()).isEqualTo(400);
            assertThat(result.getTitle()).isEqualTo("Bad Request");
            assertThat(result.getType()).isEqualTo(URI.create("about:blank"));
            assertThat(result.getProperties())
                    .containsEntry("error_code", "BAD_REQUEST")
                    .containsKey("timestamp");
            assertThat(result.getProperties()).extractingByKey("timestamp").isInstanceOf(Instant.class);
        }
    }

    @Nested
    class Detail {

        @Test
        void detail_WithNonNullNonBlankString_SetsDetail() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.CONFLICT, "CONFLICT", "Conflict")
                    .detail("Detailed error message")
                    .build();

            assertThat(result.getDetail()).isEqualTo("Detailed error message");
        }

        @Test
        void detail_WithNullString_DoesNotSetDetail() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.CONFLICT, "CONFLICT", "Conflict")
                    .detail(null)
                    .build();

            assertThat(result.getDetail()).isNull();
        }

        @Test
        void detail_WithBlankString_DoesNotSetDetail() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.CONFLICT, "CONFLICT", "Conflict")
                    .detail("   ")
                    .build();

            assertThat(result.getDetail()).isNull();
        }
    }

    @Nested
    class Type {

        @Test
        void type_WithNonNullUri_SetsType() {
            URI customType = URI.create("https://example.com/errors/conflict");
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.CONFLICT, "CONFLICT", "Conflict")
                    .type(customType)
                    .build();

            assertThat(result.getType()).isEqualTo(customType);
        }

        @Test
        void type_WithNullUri_KeepsDefaultType() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.CONFLICT, "CONFLICT", "Conflict")
                    .type(null)
                    .build();

            assertThat(result.getType()).isEqualTo(URI.create("about:blank"));
        }
    }

    @Nested
    class InvalidParams {

        @Test
        void invalidParams_WithNonEmptyMap_SetsInvalidParamsProperty() {
            Map<String, String> params = Map.of("email", "must be valid", "age", "must be positive");
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "INVALID", "Invalid")
                    .invalidParams(params)
                    .build();

            assertThat(result.getProperties()).containsEntry("invalid_params", params);
        }

        @Test
        void invalidParams_WithNullMap_DoesNotSetProperty() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "INVALID", "Invalid")
                    .invalidParams(null)
                    .build();

            assertThat(result.getProperties()).doesNotContainKey("invalid_params");
        }

        @Test
        void invalidParams_WithEmptyMap_DoesNotSetProperty() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "INVALID", "Invalid")
                    .invalidParams(Map.of())
                    .build();

            assertThat(result.getProperties()).doesNotContainKey("invalid_params");
        }
    }

    @Nested
    class With {

        @Test
        void with_WithNonReservedKey_SetsCustomProperty() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "ERR", "Error")
                    .with("trace_id", "abc-123")
                    .build();

            assertThat(result.getProperties()).containsEntry("trace_id", "abc-123");
        }

        @Test
        void with_WithNullKey_DoesNotSetProperty() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "ERR", "Error")
                    .with(null, "value")
                    .build();

            assertThat(result.getProperties()).doesNotContainEntry(null, "value");
        }

        @Test
        void with_WithBlankKey_DoesNotSetProperty() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "ERR", "Error")
                    .with("", "value")
                    .build();

            assertThat(result.getProperties()).doesNotContainEntry("", "value");
        }

        @Test
        void with_WithReservedKey_DoesNotSetProperty() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "ERR", "Error")
                    .with("type", "overwritten")
                    .build();

            assertThat(result.getType()).isEqualTo(URI.create("about:blank"));
            assertThat(result.getProperties()).doesNotContainKey("type");
        }

        @Test
        void with_WithAllReservedKeys_DoesNotOverrideExistingValues() {
            ProblemDetail result = ProblemDetailResponse
                    .of(
                            HttpStatus.BAD_REQUEST,
                            "ERR",
                            "Error")
                    .with("title", "custom title")
                    .with("status", "200")
                    .with("detail", "custom detail")
                    .with("instance", "custom instance")
                    .with("timestamp", "custom timestamp")
                    .with("error_code", "custom code")
                    .with("invalid_params", "custom params")
                    .build();

            assertThat(result.getTitle()).isEqualTo("Error");
            assertThat(result.getStatus()).isEqualTo(400);
            assertThat(result.getType()).isEqualTo(URI.create("about:blank"));
            assertThat(result.getDetail()).isNull();
            assertThat(result.getProperties())
                    .containsEntry("error_code", "ERR")
                    .doesNotContainKey("invalid_params");
            assertThat(result.getProperties()).extractingByKey("timestamp")
                    .isInstanceOf(Instant.class);
        }
    }

    @Nested
    class WithAll {

        @Test
        void withAll_WithNonEmptyMap_SetsAllCustomProperties() {
            Map<String, Object> extra = Map.of(
                    "trace_id", "abc-123",
                    "retryable", true,
                    "retry_after_ms", 5000
            );
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.SERVICE_UNAVAILABLE, "RETRY", "Retry")
                    .withAll(extra)
                    .build();

            assertThat(result.getProperties())
                    .containsEntry("trace_id", "abc-123")
                    .containsEntry("retryable", true)
                    .containsEntry("retry_after_ms", 5000);
        }

        @Test
        void withAll_WithMapContainingReservedKeys_SkipsReservedKeys() {
            Map<String, Object> mixed = Map.of(
                    "type", "https://custom",
                    "trace_id", "xyz-789",
                    "status", "999"
            );
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "ERR", "Error")
                    .withAll(mixed)
                    .build();

            assertThat(result.getType()).isEqualTo(URI.create("about:blank"));
            assertThat(result.getProperties())
                    .containsEntry("trace_id", "xyz-789")
                    .doesNotContainKey("type")
                    .doesNotContainKey("status");
        }

        @Test
        void withAll_WithNullMap_DoesNothing() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "ERR", "Error")
                    .withAll(null)
                    .build();

            assertThat(result.getProperties()).containsEntry("error_code", "ERR");
        }

        @Test
        void withAll_WithEmptyMap_DoesNothing() {
            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "ERR", "Error")
                    .withAll(Map.of())
                    .build();

            assertThat(result.getProperties()).containsEntry("error_code", "ERR");
        }
    }

    @Nested
    class BuilderChaining {

        @Test
        void allBuilderMethods_ReturnSameBuilderInstance_ForChaining() {
            ProblemDetailResponse.ProblemBuilder builder = ProblemDetailResponse.of(
                    HttpStatus.BAD_REQUEST, "ERR", "Error");

            assertThat(builder.detail("detail")).isSameAs(builder);
            assertThat(builder.type(URI.create("https://test"))).isSameAs(builder);
            assertThat(builder.invalidParams(Map.of("key", "val"))).isSameAs(builder);
            assertThat(builder.with("custom", "val")).isSameAs(builder);
            assertThat(builder.withAll(Map.of("k", "v"))).isSameAs(builder);
        }
    }

    @Nested
    class Build {

        @Test
        void fullChain_Build_ReturnsCompleteProblemDetail() {
            Map<String, String> invalidParams = Map.of("email", "must be a valid email");
            URI customType = URI.create("https://example.com/errors/validation");
            Map<String, Object> extras = Map.of(
                    "trace_id", "abc-123",
                    "correlation_id", "def-456");

            ProblemDetail result = ProblemDetailResponse.of(HttpStatus.BAD_REQUEST, "VALIDATION", "Validation Failed")
                    .detail("The request contains invalid fields")
                    .type(customType)
                    .invalidParams(invalidParams)
                    .with("request_id", "req-789")
                    .withAll(extras)
                    .build();

            assertThat(result.getStatus()).isEqualTo(400);
            assertThat(result.getTitle()).isEqualTo("Validation Failed");
            assertThat(result.getDetail()).isEqualTo("The request contains invalid fields");
            assertThat(result.getType()).isEqualTo(customType);
            assertThat(result.getProperties())
                    .containsEntry("error_code", "VALIDATION")
                    .containsKey("timestamp")
                    .containsEntry("invalid_params", invalidParams)
                    .containsEntry("request_id", "req-789")
                    .containsEntry("trace_id", "abc-123")
                    .containsEntry("correlation_id", "def-456");
        }
    }
}
