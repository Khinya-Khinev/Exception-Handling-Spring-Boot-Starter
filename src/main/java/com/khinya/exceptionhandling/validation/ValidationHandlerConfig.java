package com.khinya.exceptionhandling.validation;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ResponseEntityExceptionHandler.class)
public class ValidationHandlerConfig {

    @Bean
    @ConditionalOnMissingBean(DefaultValidationExceptionHandler.class)
    DefaultValidationExceptionHandler validationExceptionHandler() {
        return new DefaultValidationExceptionHandler();
    }
}