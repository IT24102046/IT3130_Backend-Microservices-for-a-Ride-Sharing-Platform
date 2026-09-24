package com.ridelink.driver_service.config;

import com.ridelink.driver_service.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    private static final String[] PUBLIC_ENDPOINTS = {
        "/swagger-ui/**",
        "/swagger-ui.html",
        "/v3/api-docs/**",
        "/actuator/health",
        "/actuator/info"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            // JWTs are stateless - we never want Spring creating an HTTP session
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                // Ride Service calls this to find a driver - no end-user token involved
                .requestMatchers(HttpMethod.GET, "/api/v1/drivers/eligible").permitAll()
                // Driver self-service writes require a DRIVER (or ADMIN) token
                .requestMatchers(HttpMethod.POST, "/api/v1/drivers").hasAnyRole("DRIVER", "ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/drivers/**").hasAnyRole("DRIVER", "ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/v1/vehicles").hasAnyRole("DRIVER", "ADMIN")
.requestMatchers(HttpMethod.GET, "/api/v1/vehicles/**").authenticated()
                // Anything else just needs a valid, authenticated token
                .anyRequest().authenticated()
            )
            // Return JSON (matching ApiResponse shape) instead of Spring's default
            // whitelabel HTML page on 401 / 403
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"success\":false,\"message\":\"Unauthorized: valid JWT required\",\"data\":null}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"success\":false,\"message\":\"Forbidden: insufficient role\",\"data\":null}");
                })
            )
            // Plug our filter in BEFORE Spring's default username/password filter
            // so every request is checked for a valid JWT first
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}