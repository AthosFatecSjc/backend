package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.energia.backend.dto.ConsentimentoDocumentoResponse;
import com.energia.backend.dto.ConsentimentosVigentesResponse;
import com.energia.backend.dto.TermoRequest;
import com.energia.backend.dto.TermosResponse;
import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.exception.NenhumTermoPassadoException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.TermTypeEntity;
import com.energia.backend.model.TermTypeName;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.repository.TermTypeRepository;
import com.energia.backend.dto.UserTermResponse;
import com.energia.backend.model.UserTermsEntity;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRepository;

@Service
public class TermsService {

    static final String TERMS_OF_USE = "TERMS_OF_USE";
    static final String PRIVACY_POLICY = "PRIVACY_POLICY";
    static final String MARKETING_COMMUNICATION = "MARKETING_COMMUNICATION";

    private final TermsRepository termsRepository;
    private final TermTypeRepository termTypeRepository;
    private final UserTermsRepository userTermsRespository;

    public TermsService(
            TermsRepository termsRepository,
            TermTypeRepository termTypeRepository,
            UserTermsRepository userTermsRespository) {
        this.termsRepository = termsRepository;
        this.termTypeRepository = termTypeRepository;
        this.userTermsRespository = userTermsRespository;
    }

    @Transactional(readOnly = true)
    public ConsentimentosVigentesResponse buscarDocumentosVigentes() {

        LocalDateTime agora = LocalDateTime.now();

        List<TermsEntity> termosVigentes = termsRepository.findActiveByReferenceTime(agora);

        Map<String, List<TermsEntity>> termosPorTipo = termosVigentes.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTermType().getName().name().toUpperCase()));

        List<TermsEntity> terms = termosPorTipo.get(TERMS_OF_USE);
        List<TermsEntity> privacy = termosPorTipo.get(PRIVACY_POLICY);
        List<TermsEntity> marketing = termosPorTipo.get(MARKETING_COMMUNICATION);

        if (terms == null || terms.isEmpty()) {
            throw new DocumentosObrigatoriosNaoConfiguradosException(
                    "Documento obrigatorio nao configurado: " + TERMS_OF_USE);
        }

        if (privacy == null || privacy.isEmpty()) {
            throw new DocumentosObrigatoriosNaoConfiguradosException(
                    "Documento obrigatorio nao configurado: " + PRIVACY_POLICY);
        }

        return new ConsentimentosVigentesResponse(
                toResponseList(terms),
                toResponseList(privacy),
                marketing != null ? toResponseList(marketing) : null);
    }

    private List<ConsentimentoDocumentoResponse> toResponseList(List<TermsEntity> terms) {
        return terms.stream()
                .map(term -> new ConsentimentoDocumentoResponse(
                        term.getId(),
                        term.getTermType().getName().name(),
                        term.getContent(),
                        term.getTermType().getIsRequired(),
                        term.getClause()))
                .toList();
    }

    @Transactional
    public TermosResponse cadastrarNovoTermo(TermoRequest request) {

        TermTypeEntity termType = termTypeRepository
                .findByName(TermTypeName.valueOf(request.getTermTypeName()))
                .orElseThrow(() -> new RuntimeException(
                        "Tipo de termo não encontrado: " + request.getTermTypeName()));

        Integer maxClause = termsRepository.findMaxClauseByTermType(termType.getId());
        if (maxClause == null) {
            maxClause = 0;
        }
        int novaClause = maxClause + 1;

        TermsEntity novoTermo = new TermsEntity();
        novoTermo.setTermType(termType);
        novoTermo.setContent(request.getContent());
        novoTermo.setClause(novaClause);

        if (request.getEffectivityStartAt() != null) {
            novoTermo.setEffectivityStartAt(request.getEffectivityStartAt());
        } else {
            novoTermo.setEffectivityStartAt(LocalDateTime.now());
        }
        TermosResponse response = toTermosResponse(termsRepository.save(novoTermo));
        return response;
    }

    @Transactional
    public TermosResponse desativarTermo(UUID termoId) {
        TermsEntity termo = termsRepository.findById(termoId)
                .orElseThrow(() -> new RuntimeException("Termo não encontrado: " + termoId));

        if (termo.getEffectivityEndAt() != null) {
            throw new RuntimeException("Termo já está encerrado");
        }

        termo.setEffectivityEndAt(LocalDateTime.now());
        TermosResponse response = toTermosResponse(termsRepository.save(termo));
        return response;
    }

    @Transactional
    public TermosResponse editarTermo(UUID termoId, TermoRequest request) {

        TermsEntity termoAtual = termsRepository.findById(termoId)
                .orElseThrow(() -> new RuntimeException("Termo não encontrado: " + termoId));

        desativarTermo(termoId);

        TermsEntity novoTermo = new TermsEntity();
        novoTermo.setTermType(termoAtual.getTermType());
        novoTermo.setContent(request.getContent());
        novoTermo.setClause(termoAtual.getClause());
        novoTermo.setEffectivityStartAt(LocalDateTime.now());

        TermsEntity salvo = termsRepository.save(novoTermo);

        return toTermosResponse(salvo);
    }

    public Map<String, TermosResponse> carregarTermosVigentesPorTipo(boolean apenasObrigatorios) {

        List<TermsEntity> termos = apenasObrigatorios
                ? termsRepository.findActiveRequiredByReferenceTime(LocalDateTime.now())
                : termsRepository.findActiveByReferenceTime(LocalDateTime.now());

        return termos.stream()
                .collect(Collectors.toMap(
                        t -> t.getTermType().getName() + "_" + t.getClause(),
                        t -> toTermosResponse(t)));
    }

    public void validarTermosEnviados(List<UUID> termosIds) {
        if (termosIds == null || termosIds.isEmpty()) {
            throw new NenhumTermoPassadoException("Lista de termos nao pode ser vazia.");
        }

        Set<UUID> termosEnviadosIds = new HashSet<>(termosIds);
        List<TermsEntity> termosEnviados = termsRepository.findAllById(termosEnviadosIds);

        if (termosEnviados.size() != termosEnviadosIds.size()) {
            throw new TermoNaoEncontradoException(
                    "Um ou mais termos informados nao existem.");
        }

        Set<UUID> idsVigentes = termsRepository
                .findActiveByReferenceTime(LocalDateTime.now())
                .stream()
                .map(TermsEntity::getId)
                .collect(Collectors.toSet());

        for (UUID idEnviado : termosEnviadosIds) {
            if (!idsVigentes.contains(idEnviado)) {
                throw new TermoNaoEncontradoException(
                        "Termo enviado nao é vigente");
            }
        }

    }

    public TermosResponse toTermosResponse(TermsEntity item) {
        return new TermosResponse(
                item.getId(),
                item.getTermType().getName().name(),
                item.getTermType().getIsRequired(),
                item.getContent(),
                item.getClause(),
                item.getEffectivityStartAt(),
                item.getEffectivityEndAt());
    }

}
