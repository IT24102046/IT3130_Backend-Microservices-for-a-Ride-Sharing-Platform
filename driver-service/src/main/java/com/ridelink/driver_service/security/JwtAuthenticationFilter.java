package com.ridelink.driver_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Runs once per request. Reads the "Authorization: Bearer <token>" header,
 * validates it with JwtService (the token is issued by Account Service),
 * and - if valid - puts an authenticated principal into the SecurityContext
 * so that SecurityConfig's authorizeHttpRequests()/hasRole() rules can use it.
 *
 * If there's no header, or the token is invalid/expired, we simply do NOT
 * authenticate the request and let it continue - SecurityConfig then decides
 * whether the endpoint requires auth (returns 401/403) or is public.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER_NAME = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader(HEADER_NAME);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(BEARER_PREFIX.length());

        try {
            if (jwtService.isTokenValid(token) && SecurityContextHolder.getContext().getAuthentication() == null) {
                String username = jwtService.extractUsername(token);
                String role = jwtService.extractRole(token);
                String userId = jwtService.extractUserId(token);

                List<GrantedAuthority> authorities = (role == null || role.isBlank())
                        ? List.of()
                        : List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));

                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                if (userId != null) {
                    request.setAttribute("authUserId", userId);
                }

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception ex) {
            // Malformed / expired / tampered token -> treat as anonymous.
            // SecurityConfig will reject it with 401 if the endpoint needs auth.
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}