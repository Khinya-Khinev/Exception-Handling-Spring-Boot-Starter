package com.khinya.exceptionhandling;

import com.khinya.exceptionhandling.application.AppExceptionHandlerConfig;
import com.khinya.exceptionhandling.application.DefaultAppExceptionHandler;
import com.khinya.exceptionhandling.mvc.DefaultWebMvcExceptionHandler;
import com.khinya.exceptionhandling.mvc.WebMvcHandlerConfig;
import com.khinya.exceptionhandling.security.DefaultSecurityExceptionHandler;
import com.khinya.exceptionhandling.security.SecurityExceptionHandlerConfig;
import com.khinya.exceptionhandling.security.entrypoints.DefaultAccessDeniedHandler;
import com.khinya.exceptionhandling.security.entrypoints.DefaultAuthenticationEntryPoint;
import com.khinya.exceptionhandling.validation.DefaultValidationExceptionHandler;
import com.khinya.exceptionhandling.validation.ValidationHandlerConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ExceptionHandlingAutoConfigurationTest {

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExceptionHandlingAutoConfiguration.class))
            .withBean("handlerExceptionResolver", HandlerExceptionResolver.class, () -> mock(HandlerExceptionResolver.class));

    private final ApplicationContextRunner nonWebContextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExceptionHandlingAutoConfiguration.class));

    private final ReactiveWebApplicationContextRunner reactiveContextRunner = new ReactiveWebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExceptionHandlingAutoConfiguration.class));

    @Nested
    class ConfigurationStructure {

        @Test
        void autoConfiguration_HasAutoConfigurationAnnotation() {
            assertThat(ExceptionHandlingAutoConfiguration.class.isAnnotationPresent(AutoConfiguration.class)).isTrue();
        }

        @Test
        void autoConfiguration_HasConditionalOnServletWebApplicationAnnotation() {
            ConditionalOnWebApplication annotation =
                    ExceptionHandlingAutoConfiguration.class.getAnnotation(ConditionalOnWebApplication.class);

            assertThat(annotation).isNotNull();
            assertThat(annotation.type()).isEqualTo(ConditionalOnWebApplication.Type.SERVLET);
        }

        @Test
        void autoConfiguration_ImportsAllExpectedConfigClasses() {
            Import importAnnotation = ExceptionHandlingAutoConfiguration.class.getAnnotation(Import.class);

            assertThat(importAnnotation).isNotNull();
            assertThat(Arrays.asList(importAnnotation.value()))
                    .containsExactlyInAnyOrder(
                            WebMvcHandlerConfig.class,
                            SecurityExceptionHandlerConfig.class,
                            ValidationHandlerConfig.class,
                            AppExceptionHandlerConfig.class
                    );
        }
    }

    @Nested
    class AutoConfigurationEnvironment {

        @Test
        void autoConfiguration_WhenInServletWebApplicationAndBeansMissing_RegistersAllHandlersAndConfigs() {
            webContextRunner.run(context -> {
                assertThat(context).hasSingleBean(ExceptionHandlingAutoConfiguration.class);
                assertThat(context).hasSingleBean(AppExceptionHandlerConfig.class);
                assertThat(context).hasSingleBean(WebMvcHandlerConfig.class);
                assertThat(context).hasSingleBean(SecurityExceptionHandlerConfig.class);
                assertThat(context).hasSingleBean(ValidationHandlerConfig.class);

                assertThat(context).hasSingleBean(DefaultAppExceptionHandler.class);
                assertThat(context).hasSingleBean(DefaultWebMvcExceptionHandler.class);
                assertThat(context).hasSingleBean(DefaultSecurityExceptionHandler.class);
                assertThat(context).hasSingleBean(DefaultValidationExceptionHandler.class);
                assertThat(context).hasSingleBean(DefaultAuthenticationEntryPoint.class);
                assertThat(context).hasSingleBean(DefaultAccessDeniedHandler.class);
            });
        }

        @Test
        void autoConfiguration_WhenNotInServletWebApplication_DoesNotRegisterAnyBeans() {
            nonWebContextRunner.run(context -> {
                assertThat(context).doesNotHaveBean(ExceptionHandlingAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(DefaultAppExceptionHandler.class);
                assertThat(context).doesNotHaveBean(DefaultWebMvcExceptionHandler.class);
                assertThat(context).doesNotHaveBean(DefaultSecurityExceptionHandler.class);
                assertThat(context).doesNotHaveBean(DefaultValidationExceptionHandler.class);
                assertThat(context).doesNotHaveBean(DefaultAuthenticationEntryPoint.class);
                assertThat(context).doesNotHaveBean(DefaultAccessDeniedHandler.class);
            });
        }

        @Test
        void autoConfiguration_WhenInReactiveWebApplication_DoesNotRegisterAnyBeans() {
            reactiveContextRunner.run(context -> {
                assertThat(context).doesNotHaveBean(ExceptionHandlingAutoConfiguration.class);
                assertThat(context).doesNotHaveBean(DefaultAppExceptionHandler.class);
                assertThat(context).doesNotHaveBean(DefaultWebMvcExceptionHandler.class);
                assertThat(context).doesNotHaveBean(DefaultSecurityExceptionHandler.class);
                assertThat(context).doesNotHaveBean(DefaultValidationExceptionHandler.class);
                assertThat(context).doesNotHaveBean(DefaultAuthenticationEntryPoint.class);
                assertThat(context).doesNotHaveBean(DefaultAccessDeniedHandler.class);
            });
        }
    }

    @Nested
    class ConditionalOnMissingBeanBackOff {

        @Test
        void appExceptionHandler_WhenCustomBeanPresent_BacksOff() {
            DefaultAppExceptionHandler customHandler = new DefaultAppExceptionHandler();

            webContextRunner
                    .withBean(DefaultAppExceptionHandler.class, () -> customHandler)
                    .run(context -> {
                        assertThat(context).hasSingleBean(DefaultAppExceptionHandler.class);
                        assertThat(context.getBean(DefaultAppExceptionHandler.class)).isSameAs(customHandler);
                    });
        }

        @Test
        void webMvcExceptionHandler_WhenCustomBeanPresent_BacksOff() {
            DefaultWebMvcExceptionHandler customHandler = new DefaultWebMvcExceptionHandler();

            webContextRunner
                    .withBean(DefaultWebMvcExceptionHandler.class, () -> customHandler)
                    .run(context -> {
                        assertThat(context).hasSingleBean(DefaultWebMvcExceptionHandler.class);
                        assertThat(context.getBean(DefaultWebMvcExceptionHandler.class)).isSameAs(customHandler);
                    });
        }

        @Test
        void securityExceptionHandler_WhenCustomBeanPresent_BacksOff() {
            DefaultSecurityExceptionHandler customHandler = new DefaultSecurityExceptionHandler();

            webContextRunner
                    .withBean(DefaultSecurityExceptionHandler.class, () -> customHandler)
                    .run(context -> {
                        assertThat(context).hasSingleBean(DefaultSecurityExceptionHandler.class);
                        assertThat(context.getBean(DefaultSecurityExceptionHandler.class)).isSameAs(customHandler);
                    });
        }

        @Test
        void customAuthenticationEntryPoint_WhenCustomBeanPresent_BacksOff() {
            DefaultAuthenticationEntryPoint customEntryPoint = new DefaultAuthenticationEntryPoint(mock(HandlerExceptionResolver.class));

            webContextRunner
                    .withBean(DefaultAuthenticationEntryPoint.class, () -> customEntryPoint)
                    .run(context -> {
                        assertThat(context).hasSingleBean(DefaultAuthenticationEntryPoint.class);
                        assertThat(context.getBean(DefaultAuthenticationEntryPoint.class)).isSameAs(customEntryPoint);
                    });
        }

        @Test
        void customAccessDeniedHandler_WhenCustomBeanPresent_BacksOff() {
            DefaultAccessDeniedHandler customHandler = new DefaultAccessDeniedHandler(mock(HandlerExceptionResolver.class));

            webContextRunner
                    .withBean(DefaultAccessDeniedHandler.class, () -> customHandler)
                    .run(context -> {
                        assertThat(context).hasSingleBean(DefaultAccessDeniedHandler.class);
                        assertThat(context.getBean(DefaultAccessDeniedHandler.class)).isSameAs(customHandler);
                    });
        }

        @Test
        void validationExceptionHandler_WhenCustomBeanPresent_BacksOff() {
            DefaultValidationExceptionHandler customHandler = new DefaultValidationExceptionHandler();

            webContextRunner
                    .withBean(DefaultValidationExceptionHandler.class, () -> customHandler)
                    .run(context -> {
                        assertThat(context).hasSingleBean(DefaultValidationExceptionHandler.class);
                        assertThat(context.getBean(DefaultValidationExceptionHandler.class)).isSameAs(customHandler);
                    });
        }

        @Test
        void allHandlers_WhenAllCustomBeansPresent_AllDefaultHandlersBackOff() {
            DefaultAppExceptionHandler customAppHandler = new DefaultAppExceptionHandler();
            DefaultWebMvcExceptionHandler customMvcHandler = new DefaultWebMvcExceptionHandler();
            DefaultSecurityExceptionHandler customSecurityHandler = new DefaultSecurityExceptionHandler();
            DefaultValidationExceptionHandler customValidationHandler = new DefaultValidationExceptionHandler();
            DefaultAuthenticationEntryPoint customEntryPoint = new DefaultAuthenticationEntryPoint(mock(HandlerExceptionResolver.class));
            DefaultAccessDeniedHandler customDeniedHandler = new DefaultAccessDeniedHandler(mock(HandlerExceptionResolver.class));

            webContextRunner
                    .withBean(DefaultAppExceptionHandler.class, () -> customAppHandler)
                    .withBean(DefaultWebMvcExceptionHandler.class, () -> customMvcHandler)
                    .withBean(DefaultSecurityExceptionHandler.class, () -> customSecurityHandler)
                    .withBean(DefaultValidationExceptionHandler.class, () -> customValidationHandler)
                    .withBean(DefaultAuthenticationEntryPoint.class, () -> customEntryPoint)
                    .withBean(DefaultAccessDeniedHandler.class, () -> customDeniedHandler)
                    .run(context -> {
                        assertThat(context).hasSingleBean(DefaultAppExceptionHandler.class);
                        assertThat(context.getBean(DefaultAppExceptionHandler.class)).isSameAs(customAppHandler);

                        assertThat(context).hasSingleBean(DefaultWebMvcExceptionHandler.class);
                        assertThat(context.getBean(DefaultWebMvcExceptionHandler.class)).isSameAs(customMvcHandler);

                        assertThat(context).hasSingleBean(DefaultSecurityExceptionHandler.class);
                        assertThat(context.getBean(DefaultSecurityExceptionHandler.class)).isSameAs(customSecurityHandler);

                        assertThat(context).hasSingleBean(DefaultValidationExceptionHandler.class);
                        assertThat(context.getBean(DefaultValidationExceptionHandler.class)).isSameAs(customValidationHandler);

                        assertThat(context).hasSingleBean(DefaultAuthenticationEntryPoint.class);
                        assertThat(context.getBean(DefaultAuthenticationEntryPoint.class)).isSameAs(customEntryPoint);

                        assertThat(context).hasSingleBean(DefaultAccessDeniedHandler.class);
                        assertThat(context.getBean(DefaultAccessDeniedHandler.class)).isSameAs(customDeniedHandler);
                    });
        }
    }

    @Nested
    class ConditionalOnClassMissing {

        @Test
        void securityExceptionHandlerConfig_WhenSecurityClassMissing_DoesNotRegisterSecurityBeans() {
            webContextRunner
                    .withClassLoader(new FilteredClassLoader(AuthenticationEntryPoint.class))
                    .run(context -> {
                        assertThat(context).doesNotHaveBean(SecurityExceptionHandlerConfig.class);
                        assertThat(context).doesNotHaveBean(DefaultSecurityExceptionHandler.class);
                        assertThat(context).doesNotHaveBean(DefaultAuthenticationEntryPoint.class);
                        assertThat(context).doesNotHaveBean(DefaultAccessDeniedHandler.class);
                        assertThat(context).hasSingleBean(DefaultAppExceptionHandler.class);
                    });
        }

        @Test
        void mvcHandlers_WhenResponseEntityExceptionHandlerMissing_DoesNotRegisterMvcBeans() {
            webContextRunner
                    .withClassLoader(new FilteredClassLoader(ResponseEntityExceptionHandler.class))
                    .run(context -> {
                        assertThat(context).doesNotHaveBean(WebMvcHandlerConfig.class);
                        assertThat(context).doesNotHaveBean(DefaultWebMvcExceptionHandler.class);
                        assertThat(context).doesNotHaveBean(AppExceptionHandlerConfig.class);
                        assertThat(context).doesNotHaveBean(DefaultAppExceptionHandler.class);
                        assertThat(context).doesNotHaveBean(ValidationHandlerConfig.class);
                        assertThat(context).doesNotHaveBean(DefaultValidationExceptionHandler.class);
                    });
        }
    }

    @Nested
    class AutoConfigurationImports {

        @Test
        void importsFile_WhenChecked_ListsAutoConfigurationClass() throws Exception {
            var res = new ClassPathResource(
                    "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
            assertThat(res.getContentAsString(StandardCharsets.UTF_8))
                    .contains(ExceptionHandlingAutoConfiguration.class.getName());
        }
    }
}
