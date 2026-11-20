package com.khinya.exceptionhandling.security;

import com.khinya.exceptionhandling.security.entrypoints.DefaultAccessDeniedHandler;
import com.khinya.exceptionhandling.security.entrypoints.DefaultAuthenticationEntryPoint;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({AuthenticationEntryPoint.class, HandlerExceptionResolver.class})
public class SecurityExceptionHandlerConfig {

    @Bean
    @ConditionalOnMissingBean(DefaultSecurityExceptionHandler.class)
    DefaultSecurityExceptionHandler securityExceptionHandler() {
        return new DefaultSecurityExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean(DefaultAuthenticationEntryPoint.class)
    DefaultAuthenticationEntryPoint customAuthenticationEntryPoint(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        return new DefaultAuthenticationEntryPoint(resolver);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultAccessDeniedHandler.class)
    DefaultAccessDeniedHandler customAccessDeniedHandler(
            @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver
    ) {
        return new DefaultAccessDeniedHandler(resolver);
    }
}
