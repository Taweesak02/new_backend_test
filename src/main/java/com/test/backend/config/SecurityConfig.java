package com.test.backend.config;


import com.test.backend.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final HandlerExceptionResolver handlerExceptionResolver;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // public routes
                        .requestMatchers("/api/auth/register", "/api/auth/login","/api/auth/refresh").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**",
                                "/v3/api-docs").permitAll()
                        // admin only
                        .requestMatchers(HttpMethod.GET, "/api/users").hasAuthority("admin")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("admin")
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/status").hasAuthority("admin")
                        // any logged-in user
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex-> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            handlerExceptionResolver.resolveException(
                                    request, response, null,
                                    new BadCredentialsException(authException.getMessage())
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            handlerExceptionResolver.resolveException(
                                    request, response, null,
                                    new AccessDeniedException(accessDeniedException.getMessage()) // AccessDeniedException — already handled
                            );
                        })
                )

                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}