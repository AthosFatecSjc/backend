package com.energia.backend.security;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.repository.UsuarioRepository;
import com.energia.backend.service.TermsService;
import com.energia.backend.service.TermsUserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class TermsAcceptanceGateFilter extends OncePerRequestFilter {

    private final TermsUserService termsUserService;
    private final UsuarioRepository userRepository;

    public TermsAcceptanceGateFilter(TermsService termsService,
            TermsUserService termsUserService,
            UsuarioRepository userRepository) {
        this.termsUserService = termsUserService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || isAllowedPath(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            UUID userId = UUID.fromString(authentication.getName());
            AppUserEntity user = userRepository.findById(userId).orElse(null);
            if (!termsUserService.checkRequiredTerms(null, user, LocalDateTime.now())) {
                response.setStatus(428);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write("""
                        {"code":"TERMS_ACCEPTANCE_REQUIRED","message":"Existem termos vigentes pendentes. Direcione o usuario para a tela de consentimento antes do acesso ao modulo."}
                        """);
                return;
            }
        } catch (IllegalArgumentException ex) {
            // Ignore malformed principal and let the rest of the security chain handle it.
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAllowedPath(String path) {
        return path == null
                || path.startsWith("/auth/")
                || path.startsWith("/documentos/consentimentos/vigentes")
                || path.startsWith("/usuarios/meus-termos")
                || path.startsWith("/usuarios/minha-conta")
                || path.startsWith("/usuarios/login-sharing")
                || path.startsWith("/terms");
    }
}
