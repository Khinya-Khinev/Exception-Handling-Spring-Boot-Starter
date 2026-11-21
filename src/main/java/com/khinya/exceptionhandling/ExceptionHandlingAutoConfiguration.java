package com.khinya.exceptionhandling;

import com.khinya.exceptionhandling.application.AppExceptionHandlerConfig;
import com.khinya.exceptionhandling.mvc.WebMvcHandlerConfig;
import com.khinya.exceptionhandling.security.SecurityExceptionHandlerConfig;
import com.khinya.exceptionhandling.validation.ValidationHandlerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({
        WebMvcHandlerConfig.class,
        SecurityExceptionHandlerConfig.class,
        ValidationHandlerConfig.class,
        AppExceptionHandlerConfig.class
})
public class ExceptionHandlingAutoConfiguration {}