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
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/terms/pending/resolve").permitAll()
                .requestMatchers("/login").permitAll()
                // .requestMatchers("/terms/**").permitAll()
                .requestMatchers("/usuarios/cadastro").permitAll()
                .requestMatchers("/documentos/consentimentos/vigentes").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/usuarios/**").authenticated()
                .requestMatchers("/indicadores/**").authenticated()
                .requestMatchers("/concessionarias/**").authenticated()
                .anyRequest().authenticated()
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
}
