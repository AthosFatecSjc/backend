package com.energia.backend.service;

import com.energia.backend.dto.ConsentimentoDocumentoResponse;
import com.energia.backend.dto.ConsentimentosVigentesResponse;
import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermsJpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConsentimentoVigenteService {
    static final String TERMS_OF_USE = "TERMS_OF_USE";
    static final String PRIVACY_POLICY = "PRIVACY_POLICY";
    static final String MARKETING_COMMUNICATION = "MARKETING_COMMUNICATION";

    private final TermsJpaRepository termsRepository;

    public ConsentimentoVigenteService(TermsJpaRepository termsRepository) {
        this.termsRepository = termsRepository;
    }

    @Transactional(readOnly = true)
    public ConsentimentosVigentesResponse buscarDocumentosVigentes() {
        LocalDateTime agora = LocalDateTime.now();

        ConsentimentoDocumentoResponse terms = mapRequired(TERMS_OF_USE, true, agora);
        ConsentimentoDocumentoResponse privacy = mapRequired(PRIVACY_POLICY, true, agora);
        ConsentimentoDocumentoResponse marketing = mapOptional(MARKETING_COMMUNICATION, false, agora);

        return new ConsentimentosVigentesResponse(terms, privacy, marketing);
    }

    private ConsentimentoDocumentoResponse mapRequired(String typeName, boolean required, LocalDateTime referenceTime) {
        TermsEntity term = termsRepository.findActiveByTypeName(typeName, referenceTime)
                .stream()
                .findFirst()
                .orElseThrow(() -> new DocumentosObrigatoriosNaoConfiguradosException(
                        "Documento obrigatorio nao configurado: " + typeName + "."
                ));

        return toResponse(term, required);
    }

    private ConsentimentoDocumentoResponse mapOptional(String typeName, boolean required, LocalDateTime referenceTime) {
        return termsRepository.findActiveByTypeName(typeName, referenceTime)
                .stream()
                .findFirst()
                .map(term -> toResponse(term, required))
                .orElse(null);
    }

    private ConsentimentoDocumentoResponse toResponse(TermsEntity term, boolean required) {
        return new ConsentimentoDocumentoResponse(
                term.getId(),
                term.getTermType().getName(),
                term.getVersion(),
                term.getContent(),
                required
        );
    }
}
