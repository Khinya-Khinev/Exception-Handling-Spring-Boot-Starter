package com.khinya.exceptionhandling.helpers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@UtilityClass
public final class ProblemDetailResponse {

    private static final URI DEFAULT_TYPE = URI.create("about:blank");
    private static final Set<String> RESERVED_KEYS = Set.of(
            "type", "title", "status", "detail", "instance",
            "timestamp", "error_code", "invalid_params"
    );

    public static ProblemBuilder of(HttpStatusCode status, String errorCode, String title) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setType(DEFAULT_TYPE);
        pd.setTitle(title);
        resolveInstance().ifPresent(pd::setInstance);

        pd.setProperty("error_code", errorCode);
        pd.setProperty("timestamp", Instant.now());

        return new ProblemBuilder(pd);
    }

    private static Optional<URI> resolveInstance() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            HttpServletRequest request = attrs.getRequest();

            try {
                return Optional.of(URI.create(request.getRequestURI()));
            } catch (IllegalArgumentException ex) {
                log.debug("Could not parse request URI '{}' as ProblemDetail instance", request.getRequestURI(), ex);
            }
        }
        return Optional.empty();
    }

    public static final class ProblemBuilder {
        private final ProblemDetail pd;

        private ProblemBuilder(ProblemDetail pd) {
            this.pd = pd;
        }

        public ProblemBuilder detail(String detail) {
            if (detail != null && !detail.isBlank()) {
                pd.setDetail(detail);
            }
            return this;
        }

        public ProblemBuilder type(URI type) {
            if (type != null) {
                pd.setType(type);
            }
            return this;
        }

        public ProblemBuilder invalidParams(Map<String, String> invalidParams) {
            if (invalidParams != null && !invalidParams.isEmpty()) {
                pd.setProperty("invalid_params", invalidParams);
            }
            return this;
        }

        public ProblemBuilder with(String key, Object value) {
            if (key == null || key.isBlank()) {
                return this;
            }
            if (RESERVED_KEYS.contains(key)) {
                return this;
            }
            pd.setProperty(key, value);
            return this;
        }

        public ProblemBuilder withAll(Map<String, ?> properties) {
            if (properties != null) {
                properties.forEach(this::with);
            }
            return this;
        }

        public ProblemDetail build() {
            return pd;
        }
    }
}