package com.ridelink.fare_payment_service.security;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    public SecurityConfig(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Creates the JWT authentication filter.
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {

        return new JwtAuthenticationFilter(jwtService);
    }

    /**
     * Spring Security configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            /*
             * CSRF is not required for our stateless
             * REST API.
             */
            .csrf(csrf -> csrf.disable())

            /*
             * This is a backend microservice.
             */
            .cors(cors -> cors.disable())

            /*
             * JWT authentication is stateless.
             */
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS
                    )
            )

            /*
             * API authorization rules.
             */
            .authorizeHttpRequests(auth -> auth

                    /*
                     * Swagger endpoints must remain accessible
                     * so we can use Swagger UI for testing.
                     */
                    .requestMatchers(
                            "/swagger-ui/**",
                            "/swagger-ui.html",
                            "/v3/api-docs/**"
                    ).permitAll()

                    /*
                     * Health check.
                     */
                    .requestMatchers(
                            "/actuator/health"
                    ).permitAll()

                    /*
                     * Fare, payment and receipt APIs
                     * require authentication.
                     */
                    .requestMatchers(
                            "/api/fares/**",
                            "/api/payments/**",
                            "/api/receipts/**"
                    ).authenticated()

                    /*
                     * Everything else requires authentication.
                     */
                    .anyRequest().authenticated()
            )

            /*
             * Authentication / authorization errors.
             */
            .exceptionHandling(exceptions -> exceptions

                    /*
                     * 401 = no valid authentication.
                     */
                    .authenticationEntryPoint(
                            (request, response, authException) -> {

                                response.setStatus(
                                        HttpServletResponse
                                                .SC_UNAUTHORIZED
                                );

                                response.getWriter().write(
                                        "Unauthorized - JWT missing or invalid"
                                );
                            }
                    )

                    /*
                     * 403 = authenticated but insufficient role.
                     */
                    .accessDeniedHandler(
                            (request, response,
                             accessDeniedException) -> {

                                response.setStatus(
                                        HttpServletResponse
                                                .SC_FORBIDDEN
                                );

                                response.getWriter().write(
                                        "Forbidden - insufficient permissions"
                                );
                            }
                    )
            )

            /*
             * Run our JWT filter before Spring Security's
             * username/password authentication filter.
             */
            .addFilterBefore(
                    jwtAuthenticationFilter(),
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}