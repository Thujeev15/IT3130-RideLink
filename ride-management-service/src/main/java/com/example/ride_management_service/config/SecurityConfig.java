package com.example.ride_management_service.config;

import com.example.ride_management_service.security.JwtAuthenticationFilter;
import com.example.ride_management_service.security.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, exception) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN)))

                .authorizeHttpRequests(auth -> auth

                        // Swagger / OpenAPI
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api-docs/**"
                        ).permitAll()

                        // Passenger / Admin
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/rides"
                        ).hasAnyRole("PASSENGER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rides/passenger/**"
                        ).hasAnyRole("PASSENGER", "ADMIN")

                        // Driver / Admin
                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/rides/driver/**"
                        ).hasAnyRole("DRIVER", "ADMIN")

                        .requestMatchers(
                                HttpMethod.PUT,
                                "/api/rides/*/accept",
                                "/api/rides/*/start",
                                "/api/rides/*/complete"
                        ).hasAnyRole("DRIVER", "ADMIN")

                        // All remaining Ride APIs require authentication
                        .requestMatchers("/api/rides/**").authenticated()

                        .anyRequest().authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}