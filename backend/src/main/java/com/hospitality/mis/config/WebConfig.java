package com.hospitality.mis.config;



import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.web.cors.CorsConfiguration;

import org.springframework.web.cors.CorsConfigurationSource;

import org.springframework.web.cors.UrlBasedCorsConfigurationSource;



import java.util.Arrays;

import java.util.List;



@Configuration

public class WebConfig {



    @Bean

    CorsConfigurationSource corsConfigurationSource(

            @Value("${CORS_ALLOWED_ORIGINS}") String configuredOrigins) {

        List<String> origins = Arrays.stream(configuredOrigins.split(","))

                .map(String::trim)

                .filter(origin -> !origin.isBlank())

                .toList();

        if (origins.isEmpty() || origins.stream().anyMatch(origin -> origin.equals("*") || origin.contains("*"))) {

            throw new IllegalStateException("CORS_ALLOWED_ORIGINS must contain explicit origins and cannot contain '*'");

        }



        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(origins);

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "Idempotency-Key"));

        configuration.setExposedHeaders(List.of("Location"));

        configuration.setAllowCredentials(false);

        configuration.setMaxAge(3600L);



        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;

    }

}
