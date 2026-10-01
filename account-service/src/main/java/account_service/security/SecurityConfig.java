package account_service.security;

import account_service.repository.AccountRepository;
import account_service.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.Instant;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
        @Bean
        ObjectMapper objectMapper() { return new ObjectMapper().findAndRegisterModules(); }

    @Bean
    PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService, AccountRepository accountRepository) {
        return new JwtAuthenticationFilter(jwtService, accountRepository);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                            ObjectMapper objectMapper) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/accounts/register", "/api/accounts/login").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/accounts/passenger/**").hasRole("PASSENGER")
                        .requestMatchers("/api/accounts/driver/**").hasRole("DRIVER")
                        .requestMatchers("/api/accounts/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> {
                            writeError(response, objectMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                    "Authentication is required", request.getRequestURI());
                        })
                        .accessDeniedHandler((request, response, exception) -> {
                            writeError(response, objectMapper, HttpServletResponse.SC_FORBIDDEN,
                                    "Required role is missing", request.getRequestURI());
                        }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void writeError(HttpServletResponse response, ObjectMapper objectMapper,
                            int status, String message, String path) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(new ErrorResponse(
                Instant.now(), status, status == 401 ? "Unauthorized" : "Forbidden", message, path)));
    }
}
