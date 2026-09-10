package com.hospitality.mis.config;



import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpMethod;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jwt.JwtDecoder;

import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import org.springframework.security.web.AuthenticationEntryPoint;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.security.web.access.AccessDeniedHandler;



import static org.springframework.security.config.Customizer.withDefaults;



@Configuration

@EnableMethodSecurity

public class SecurityConfig {



    @Bean

    SecurityFilterChain securityFilterChain(HttpSecurity http,

                                            JwtDecoder jwtDecoder,

                                            JwtAuthenticationConverter jwtAuthenticationConverter,

                                            AuthenticationEntryPoint authenticationEntryPoint,

                                            AccessDeniedHandler accessDeniedHandler,
                                            com.hospitality.mis.service.governance.SecurityAuditService securityAudit) throws Exception {

        http
                .addFilterBefore(new com.hospitality.mis.middleware.security.SecurityAuditFilter(securityAudit),
                        org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter.class)

                .csrf(AbstractHttpConfigurer::disable)

                .cors(withDefaults())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .httpBasic(AbstractHttpConfigurer::disable)

                .formLogin(AbstractHttpConfigurer::disable)

                .logout(AbstractHttpConfigurer::disable)

                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint(authenticationEntryPoint)

                        .accessDeniedHandler(accessDeniedHandler))

                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers(

                                "/actuator/health",

                                "/actuator/health/**",

                                "/v3/api-docs",

                                "/v3/api-docs/**",

                                "/swagger-ui.html",

                                "/swagger-ui/**",

                                "/webjars/**",

                                "/api/auth/login",

                                "/api/auth/customers/login",

                                "/api/auth/refresh",

                                "/api/auth/customers/register"

                        ).permitAll()

                        .anyRequest().authenticated())

                .oauth2ResourceServer(resourceServer -> resourceServer

                        .authenticationEntryPoint(authenticationEntryPoint)

                        .jwt(jwt -> jwt

                                .decoder(jwtDecoder)

                                .jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();

    }



    @Bean

    AuthenticationEntryPoint authenticationEntryPoint() {

        return (request, response, exception) -> writeError(response, HttpServletResponse.SC_UNAUTHORIZED);

    }



    @Bean

    AccessDeniedHandler accessDeniedHandler() {
        return (request, response, exception) -> writeError(response, HttpServletResponse.SC_FORBIDDEN,
                "ACCESS_DENIED");
    }

    private static void writeError(HttpServletResponse response, int status) throws java.io.IOException {
        writeError(response, status, "AUTHENTICATION_REQUIRED");
    }

    private static void writeError(HttpServletResponse response, int status, String code)
            throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + code + "\"}");
    }
}
