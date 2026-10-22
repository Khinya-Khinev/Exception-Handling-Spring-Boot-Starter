package com.khinya.exceptionhandlingspringbootstarter;

import com.khinya.exceptionhandlingspringbootstarter.app_exception.DefaultAppExceptionHandler;
import com.khinya.exceptionhandlingspringbootstarter.factory.DefaultProblemDetailFactory;
import com.khinya.exceptionhandlingspringbootstarter.mvc.WebMvcHandlerConfig;
import com.khinya.exceptionhandlingspringbootstarter.security.SecurityExceptionHandlerConfig;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnWebApplication
@Import({WebMvcHandlerConfig.class, SecurityExceptionHandlerConfig.class})
public class ErrorHandlingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DefaultProblemDetailFactory problemDetailFactory() {
        return new DefaultProblemDetailFactory();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultAppExceptionHandler.class)
    DefaultAppExceptionHandler appExceptionHandler(DefaultProblemDetailFactory f) {
        return new DefaultAppExceptionHandler(f);
    }
}