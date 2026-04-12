package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.ConsentimentoDocumentoResponse;
import com.energia.backend.dto.ConsentimentosVigentesResponse;
import com.energia.backend.dto.TermoRequest;
import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermTypeRepository;
import com.energia.backend.repository.TermsJpaRepository;
import com.energia.backend.repository.TermsRepository;

@Service
public class TermsService {

    static final String TERMS_OF_USE = "TERMS_OF_USE";
    static final String PRIVACY_POLICY = "PRIVACY_POLICY";
    static final String MARKETING_COMMUNICATION = "MARKETING_COMMUNICATION";
    
    private final TermsRepository termsRepository;
    private final TermsJpaRepository termsJpaRepository;
    private final TermTypeRepository termTypeRepository;

    public TermsService(TermsRepository termsRepository, TermsJpaRepository termsJpaRepository, TermTypeRepository termTypeRepository) {
        this.termsRepository = termsRepository;
        this.termsJpaRepository = termsJpaRepository;
        this.termTypeRepository = termTypeRepository;
    }

    @Transactional(readOnly = true)
    public ConsentimentosVigentesResponse buscarDocumentosVigentes() {
        LocalDateTime agora = LocalDateTime.now();

        List<TermsEntity> termosVigentes = termsRepository.findActiveByReferenceTime(agora);

        Map<String, TermsEntity> termosPorTipo = termosVigentes.stream()
            .collect(Collectors.toMap(
                t -> t.getTermType().getName().toUpperCase(),
                t -> t,
                (existing, replacement) -> existing
            ));

        TermsEntity terms = termosPorTipo.get(TERMS_OF_USE);
        TermsEntity privacy = termosPorTipo.get(PRIVACY_POLICY);
        TermsEntity marketing = termosPorTipo.get(MARKETING_COMMUNICATION);

        // valida obrigatórios
        if (terms == null) {
            throw new DocumentosObrigatoriosNaoConfiguradosException(
                    "Documento obrigatorio nao configurado: " + TERMS_OF_USE
            );
        }

        if (privacy == null) {
            throw new DocumentosObrigatoriosNaoConfiguradosException(
                    "Documento obrigatorio nao configurado: " + PRIVACY_POLICY
            );
        }

        return new ConsentimentosVigentesResponse(
                toResponse(terms),
                toResponse(privacy),
                marketing != null ? toResponse(marketing) : null
        );
    }
    

    private ConsentimentoDocumentoResponse toResponse(TermsEntity term) {
        return new ConsentimentoDocumentoResponse(
                term.getId(),
                term.getTermType().getName(),
                term.getVersion(),
                term.getContent(),
                term.getTermType().getIsRequired()
        );
    }

    @Transactional
    public TermsEntity cadastrarNovoTermo(TermoRequest request) {
    
        TermTypeEntity termType = termTypeRepository
            .findByNameIgnoreCase(request.getTermTypeName())
            .orElseThrow(() -> new RuntimeException(
                    "Tipo de termo não encontrado: " + request.getTermTypeName()
            ));
    
        Integer maxVersion = termsJpaRepository.findMaxVersionByTermType(termType.getId());
        int novaVersao = (maxVersion == null ? 1 : maxVersion + 1);
    
        termsJpaRepository.deactivateByType(termType.getId());
    
        TermsEntity novoTermo = new TermsEntity();
        novoTermo.setTermType(termType);
        novoTermo.setContent(request.getContent());
        novoTermo.setVersion(novaVersao);
        novoTermo.setIsActive(true);
    
        if (request.getEffectivityStartAt() != null) {
            novoTermo.setEffectivityStartAt(request.getEffectivityStartAt());
        } else {
            novoTermo.setEffectivityStartAt(LocalDateTime.now());
        }
    
        return termsRepository.save(novoTermo);
    }


}
