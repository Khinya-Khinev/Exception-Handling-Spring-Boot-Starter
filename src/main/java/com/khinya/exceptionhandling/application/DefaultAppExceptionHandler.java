package com.khinya.exceptionhandling.application;

import com.khinya.exceptionhandling.helpers.ProblemDetailResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class DefaultAppExceptionHandler implements MessageSourceAware {

    private MessageSource messageSource;

    @Override
    public void setMessageSource(@NonNull MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(AppException.class)
    protected ProblemDetail handleAppException(AppException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        String code = errorCode.getCode();

        if (errorCode.getHttpStatus().is5xxServerError()) {
            log.error("Business exception: {}. Code: {}. Context: {}",
                    ex.getMessage(), code, ex.getErrorContext(), ex);
        }

        return ProblemDetailResponse.of(errorCode.getHttpStatus(), code, resolveTitle(code))
                .withAll(ex.getErrorContext())
                .build();
    }

    protected String resolveTitle(String code) {
        if (messageSource == null)
            return code;

        return messageSource.getMessage(code, null, code, LocaleContextHolder.getLocale());
    }
}