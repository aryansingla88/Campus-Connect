package com.campus.Campus_Connect.features.security;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null) {
            log.debug("JWT authentication skipped for {} {}: Authorization header missing",
                    request.getMethod(), request.getServletPath());
            filterChain.doFilter(request, response);
            return;
        }

        if (!authHeader.startsWith("Bearer ")) {
            if (authHeader != null) {
                log.debug("JWT authentication skipped for {} {}: Authorization header is not Bearer",
                        request.getMethod(), request.getServletPath());
            }
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String token = authHeader.substring(7);
            if (token.isBlank()) {
                log.debug("JWT authentication rejected for {} {}: empty bearer token",
                        request.getMethod(), request.getServletPath());
                filterChain.doFilter(request, response);
                return;
            }

            Integer userId = jwtService.extractUserId(token);

            User user = userRepository.findById(userId).orElse(null);

            if (user == null || Boolean.TRUE.equals(user.getIsBanned())) {
                log.debug("JWT authentication rejected for {} {}: user missing or banned",
                        request.getMethod(), request.getServletPath());
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            java.util.List.of(
                                    new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                            "ROLE_" + user.getRole().name())
                            )
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (JwtException | IllegalArgumentException exception) {
            log.debug("JWT authentication rejected for {} {}: {}",
                    request.getMethod(), request.getServletPath(), exception.getClass().getSimpleName());
        }

        filterChain.doFilter(request, response);
    }
}
