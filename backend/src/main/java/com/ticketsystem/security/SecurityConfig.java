package com.ticketsystem.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketsystem.core.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/** Spring Security yapılandırması — JWT tabanlı stateless kimlik doğrulama. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          ObjectMapper objectMapper) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        objectMapper.writeValueAsString(ApiResponse.error("Kimlik doğrulama gerekli."))
                    );
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write(
                        objectMapper.writeValueAsString(ApiResponse.error("Bu işlem için yetkiniz yok."))
                    );
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/users/admin").hasRole("MANAGER")
                .requestMatchers(HttpMethod.GET,   "/api/v1/users/role/*/active").hasAnyRole("AGENT", "MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/users/*/deactivate").hasRole("MANAGER")
                .requestMatchers(HttpMethod.GET,   "/api/v1/users/role/*").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/tickets/*/assign").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/tickets/*/priority-review").hasAnyRole("AGENT", "MANAGER")
                .requestMatchers(HttpMethod.GET,   "/api/v1/tickets/status/*").hasAnyRole("AGENT", "MANAGER")
                .requestMatchers(HttpMethod.GET,   "/api/v1/tickets/priority/*").hasAnyRole("AGENT", "MANAGER")
                .requestMatchers(HttpMethod.GET,   "/api/v1/tickets/unassigned").hasAnyRole("AGENT", "MANAGER")
                .requestMatchers(HttpMethod.GET,   "/api/v1/tickets/filter").hasAnyRole("AGENT", "MANAGER")
                .requestMatchers(HttpMethod.POST,  "/api/v1/registration-requests").permitAll()
                .requestMatchers(HttpMethod.GET,   "/api/v1/registration-requests").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/registration-requests/*/approve").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/registration-requests/*/reject").hasRole("MANAGER")
                .requestMatchers(HttpMethod.POST,  "/api/v1/assignment-requests").hasRole("AGENT")
                .requestMatchers(HttpMethod.GET,   "/api/v1/assignment-requests").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/assignment-requests/*/approve").hasRole("MANAGER")
                .requestMatchers(HttpMethod.PATCH, "/api/v1/assignment-requests/*/reject").hasRole("MANAGER")
                .requestMatchers("/api/v1/notifications/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
