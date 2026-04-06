package com.energia.backend.config;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.energia.backend.security.JwtAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ObjectProvider<JwtAuthenticationFilter> jwtFilterProvider) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/login").permitAll()  // Backward compatibility
                .requestMatchers("/usuarios/cadastro").permitAll()
                // Admin endpoints (require ROLE_ADMIN)
                .requestMatchers("/admin/**").hasRole("admin")
                // Protected endpoints (require authentication)
                .requestMatchers("/usuarios/**").authenticated()
                .requestMatchers("/indicadores/**").authenticated()
                .requestMatchers("/concessionarias/**").authenticated()
                // All other requests
                .anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.disable());

        // Add JWT filter only if available (lazy loaded to break circular dependency)
        jwtFilterProvider.ifAvailable(filter ->
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

