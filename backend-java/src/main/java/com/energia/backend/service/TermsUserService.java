package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.exception.NenhumTermoPassadoException;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.UserTermsAction;
import com.energia.backend.model.UserTermsEntity;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRespository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TermsUserService {

    private final TermsRepository termsRepository;
    private final UserTermsRespository userTermsRespository;

    public TermsUserService(TermsRepository termsRepository, UserTermsRespository userTermsRespository) {
        this.termsRepository = termsRepository;
        this.userTermsRespository = userTermsRespository;
    }

    public void aprovarTermos(List<UUID> termosIds, AppUserEntity userEntity) {
        validarTermosEnviados(termosIds);

        if (!checkRequiredTerms(termosIds, userEntity, LocalDateTime.now())) {
            throw new DocumentosObrigatoriosNaoConfiguradosException(
                "Termo obrigatorio vigente nao aceito"
            );
        }

        Set<UUID> termosEnviadosIds = new HashSet<>(termosIds);
        List<TermsEntity> termosEnviados = termsRepository.findAllById(termosEnviadosIds);

        validarTermosVigentes(termosEnviadosIds);

        salvarTermos(userEntity, termosEnviados, UserTermsAction.ACCEPTED);
    }

    public void revogarTermos(List<UUID> termosIds, AppUserEntity userEntity) {
        validarTermosEnviados(termosIds);

        Set<UUID> termosEnviadosIds = new HashSet<>(termosIds);
        List<TermsEntity> termosEnviados = termsRepository.findAllById(termosEnviadosIds);

        validarTermosVigentes(termosEnviadosIds);

        for (TermsEntity termo : termosEnviados) {
            if (termo.getTermType().getIsRequired()) {
                throw new IllegalStateException(
                    "Nao é permitido revogar termo obrigatorio: "
                    + termo.getTermType().getName()
                );
            }
        }

        salvarTermos(userEntity, termosEnviados, UserTermsAction.REVOKED);
    }

    public void registrarCienciaTermos(List<UUID> termosIds, AppUserEntity userEntity) {
        validarTermosEnviados(termosIds);

        Set<UUID> termosEnviadosIds = new HashSet<>(termosIds);
        List<TermsEntity> termosEnviados = termsRepository.findAllById(termosEnviadosIds);

        validarTermosVigentes(termosEnviadosIds);

        for (TermsEntity termo : termosEnviados) {
            if (termo.getTermType().getIsRequired()) {
                throw new IllegalStateException(
                    "Termo obrigatorio deve ser aceito: "
                    + termo.getTermType().getName()
                );
            }
        }

        salvarTermos(userEntity, termosEnviados, UserTermsAction.ACKNOWLEDGED);
    }

    private void validarTermosEnviados(List<UUID> termosIds) {
        if (termosIds == null || termosIds.isEmpty()) {
            throw new NenhumTermoPassadoException("Lista de termos nao pode ser vazia.");
        }

        Set<UUID> termosEnviadosIds = new HashSet<>(termosIds);
        List<TermsEntity> termosEnviados = termsRepository.findAllById(termosEnviadosIds);

        if (termosEnviados.size() != termosEnviadosIds.size()) {
            throw new TermoNaoEncontradoException(
                "Um ou mais termos informados nao existem."
                + "••• termosEnviadosIds: " + termosEnviadosIds
                + " | termosEnviados: " + termosEnviados
            );
        }
    }

    private void validarTermosVigentes(Set<UUID> termosEnviadosIds) {
        Set<UUID> idsVigentes = termsRepository
            .findActiveByReferenceTime(LocalDateTime.now())
            .stream()
            .collect(Collectors.toMap(
                t -> t.getTermType().getName(),
                t -> t,
                (existente, novo) -> existente.getVersion() > novo.getVersion() ? existente : novo
            ))
            .values()
            .stream()
            .map(TermsEntity::getId)
            .collect(Collectors.toSet());

        for (UUID idEnviado : termosEnviadosIds) {
            if (!idsVigentes.contains(idEnviado)) {
                throw new TermoNaoEncontradoException(
                    "Termo enviado nao é vigente: " + idEnviado
                    // + ";;; idsVigentes: " + idsVigentes
                    // + ";;; termosEnviadosIds: " + termosEnviadosIds
                );
            }
        }
    }

    private void salvarTermos(
        AppUserEntity userEntity,
        List<TermsEntity> termosEnviados,
        UserTermsAction action
    ) {
        try {
            for (TermsEntity termo : termosEnviados) {
                UserTermsEntity userTerms = new UserTermsEntity();
                userTerms.setUser(userEntity);
                userTerms.setTerms(termo);
                userTerms.setAction(action);
                userTerms.setActionAt(LocalDateTime.now());

                userTermsRespository.save(userTerms);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao registrar configuração dos termos.", ex);
        }
    }

    public boolean checkRequiredTerms(List<UUID> termosIds, AppUserEntity user, LocalDateTime referenceTime) {
        
        Set<UUID> enviados = (termosIds == null)
            ? new HashSet<>()
            : new HashSet<>(termosIds);

        List<UUID> obrigatoriosIds = termsRepository
        .findActiveRequiredByReferenceTime(referenceTime)
        .stream()
        .map(TermsEntity::getId)
        .toList();

        if (user != null) {
            List<UserTermsEntity> activeUserTerms = findActiveUserTermsAtTime (
                user,
                referenceTime
            );
            for (UserTermsEntity userTerm : activeUserTerms) {
                UUID termId = userTerm.getTerms().getId();
        
                if (obrigatoriosIds.contains(termId)) {
                    enviados.add(termId);
                }
            }
        }

        boolean isMissing = obrigatoriosIds.stream()
        .anyMatch(id -> !enviados.contains(id));
    
        return !isMissing;
    }

    public List<UserTermsEntity> findActiveUserTermsAtTime(
        AppUserEntity user,
        LocalDateTime referenceTime
    ) {
        List<TermsEntity> termosVigentes = termsRepository.findActiveByReferenceTime(referenceTime);

        Set<UUID> termosVigentesIds = termosVigentes.stream()
            .map(TermsEntity::getId)
            .collect(Collectors.toSet());

        List<UserTermsEntity> historico = userTermsRespository
            .findByUserAndActionAtLessThanEqual(user, referenceTime);

        Map<UUID, UserTermsEntity> ultimaAcaoPorTermo = historico
            .stream()
            .filter(ut -> termosVigentesIds.contains(ut.getTerms().getId()))
            .collect(Collectors.toMap(
                ut -> ut.getTerms().getTermType().getId(),
                ut -> ut,
                (a, b) -> a.getActionAt().isAfter(b.getActionAt()) ? a : b
            ));

        return ultimaAcaoPorTermo.values()
        .stream().filter(ut -> ut.getAction() == UserTermsAction.ACCEPTED)
        .toList();
    }


}
