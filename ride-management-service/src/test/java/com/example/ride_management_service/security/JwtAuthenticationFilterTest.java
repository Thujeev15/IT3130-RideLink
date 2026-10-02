package com.example.ride_management_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class JwtAuthenticationFilterTest {

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesAccountSubjectAndRoleFromJwt() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        Claims claims = Jwts.claims()
                .subject("42")
                .add("role", "PASSENGER")
                .build();
        when(jwtService.parse("token")).thenReturn(claims);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer token");
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("42");
        assertThat(SecurityContextHolder.getContext().getAuthentication().getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_PASSENGER");
        verify(chain).doFilter(eq(request), any());
    }

    @Test
    void rejectsInvalidJwtByLeavingRequestUnauthenticated() throws Exception {
        JwtService jwtService = mock(JwtService.class);
        when(jwtService.parse("invalid")).thenThrow(new IllegalArgumentException("invalid token"));
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtService);
        FilterChain chain = mock(FilterChain.class);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer invalid");
        filter.doFilter(request, new MockHttpServletResponse(), chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain).doFilter(eq(request), any());
    }
}
