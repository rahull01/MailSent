package com.mailsent.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebMvcSecurityConfig — WebMvc configuration for CORS and web transport security.
 *
 * <p>Enables cross-origin browser requests from the Next.js frontend application
 * while preserving strict header filtering and allowed HTTP methods.</p>
 */
@Configuration
public class WebMvcSecurityConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("http://localhost:3000")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
            .allowedHeaders("Authorization", "Content-Type", "X-Request-Id")
            .exposedHeaders("X-Request-Id")
            .maxAge(3600);
    }
}
