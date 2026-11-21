package com.khinya.exceptionhandling.mvc;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(ResponseEntityExceptionHandler.class)
public class WebMvcHandlerConfig {

    @Bean
    @ConditionalOnMissingBean(DefaultWebMvcExceptionHandler.class)
    DefaultWebMvcExceptionHandler webMvcExceptionHandler() {
        return new DefaultWebMvcExceptionHandler();
    }
}