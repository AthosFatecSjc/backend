package com.energia.backend.security;

import com.energia.backend.service.TermsService;
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
import java.util.UUID;

@Component
public class TermsAcceptanceGateFilter extends OncePerRequestFilter {

    private final TermsService termsService;

    public TermsAcceptanceGateFilter(TermsService termsService) {
        this.termsService = termsService;
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
            if (termsService.hasPendingRequiredTerms(userId)) {
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
                || path.startsWith("/usuarios/minha-conta");
    }
}
