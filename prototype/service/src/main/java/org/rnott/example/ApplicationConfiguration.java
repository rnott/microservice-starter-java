package org.rnott.example;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@EnableJpaRepositories("org.rnott.example.persistence")
public class ApplicationConfiguration {
    /*
     * JPA specific configuration.
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        return null;
    }
     */

    /*
    Jackson serialization customizations.
     */

    /**
     * Enable JDK 8 niceties such as Optional.
     *
     * @return JDK 8 module.
     */
    public Jdk8Module jdk8Module() {
        return new Jdk8Module();
    }
    /**
     * Enable JDK 8 date/time support.
     *
     * @return JDK 8 date/time module.
     */
    public JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

    /**
     * Customize JSON serialization.
     * <ul>
     *     <li>Ignore properties with NULL values</li>
     *     <li>Pretty print (might want to disable in production)</li>
     * </ul>
     *
     * @return serialization configuration
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder
                .indentOutput(true)
                .serializationInclusion(JsonInclude.Include.NON_NULL);
    }
}
