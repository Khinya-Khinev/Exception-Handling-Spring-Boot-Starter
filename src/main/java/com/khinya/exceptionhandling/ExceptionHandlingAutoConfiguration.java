package com.khinya.exceptionhandling;

import com.khinya.exceptionhandling.application.DefaultAppExceptionHandler;
import com.khinya.exceptionhandling.helpers.ProblemDetailFactory;
import com.khinya.exceptionhandling.mvc.WebMvcHandlerConfig;
import com.khinya.exceptionhandling.security.SecurityExceptionHandlerConfig;
import com.khinya.exceptionhandling.validation.ValidationHandlerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({
        WebMvcHandlerConfig.class,
        SecurityExceptionHandlerConfig.class,
        ValidationHandlerConfig.class
})
public class ExceptionHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    ProblemDetailFactory problemDetailFactory() {
        return new ProblemDetailFactory();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultAppExceptionHandler.class)
    DefaultAppExceptionHandler appExceptionHandler() {
        return new DefaultAppExceptionHandler();
    }
}