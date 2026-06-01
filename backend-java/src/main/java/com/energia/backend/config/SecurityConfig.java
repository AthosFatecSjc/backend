package com.energia.backend.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.energia.backend.security.JwtAuthenticationFilter;
import com.energia.backend.security.TermsAcceptanceGateFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            ObjectProvider<JwtAuthenticationFilter> jwtFilterProvider,
            ObjectProvider<TermsAcceptanceGateFilter> termsAcceptanceGateFilterProvider
    ) throws Exception {
        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/terms/pending/resolve").permitAll()
                .requestMatchers("/login").permitAll()
                .requestMatchers("/aneel/**").permitAll()
                .requestMatchers("/terms").permitAll()
                .requestMatchers("/terms/**").permitAll()
                .requestMatchers("/users/**").permitAll()
                .requestMatchers("/usuarios/cadastro").permitAll()
                .requestMatchers("/usuarios/login-sharing/**").permitAll()
                .requestMatchers("/documentos/consentimentos/vigentes").permitAll()
                .requestMatchers("/indicadores/mapa/criticidade").permitAll()
                .requestMatchers("/ops/emergency/emails/usuarios-nao-deletados").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/usuarios/**").authenticated()
                .requestMatchers("/indicadores/**").authenticated()
                .requestMatchers("/concessionarias/**").authenticated().anyRequest().authenticated()
            )
            .httpBasic(basic -> basic.disable());

        jwtFilterProvider.ifAvailable(filter ->
            http.addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class)
        );
        termsAcceptanceGateFilterProvider.ifAvailable(filter ->
            http.addFilterAfter(filter, JwtAuthenticationFilter.class)
        );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${CORS_ORIGINS:}") String corsOrigins) {

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With"));
        configuration.setExposedHeaders(List.of("Location"));

        if (corsOrigins != null && !corsOrigins.trim().isEmpty()) {
            configuration.setAllowedOrigins(Arrays.stream(corsOrigins.split(","))
                    .map(String::trim)
                    .filter(origin -> !origin.isEmpty())
                    .toList());
        } else {
            configuration.addAllowedOriginPattern("http://localhost:*");
            configuration.addAllowedOriginPattern("http://127.0.0.1:*");
            configuration.addAllowedOriginPattern("https://localhost:*");
            configuration.addAllowedOriginPattern("https://127.0.0.1:*");
        }

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
