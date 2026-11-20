package com.khinya.exceptionhandling;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHandlingAutoConfigurationTest {

    @Test
    void importsFileListsAutoConfig() throws Exception {
        var res = new ClassPathResource(
                "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");
        assertThat(res.getContentAsString(StandardCharsets.UTF_8))
                .contains(ExceptionHandlingAutoConfiguration.class.getName());
    }

}
