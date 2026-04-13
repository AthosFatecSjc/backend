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
import com.energia.backend.repository.UserTermsRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TermsUserService {

    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRespository;

    public TermsUserService(TermsRepository termsRepository, UserTermsRepository userTermsRespository) {
        this.termsRepository = termsRepository;
        this.userTermsRespository = userTermsRespository;
    }

    public void aprovarTermos(List<String> termosNames, AppUserEntity userEntity) {
        validarTermosEnviados(termosNames);

        if (!checkRequiredTerms(termosNames, userEntity, LocalDateTime.now())) {
            throw new DocumentosObrigatoriosNaoConfiguradosException(
                "Termo obrigatorio vigente nao aceito"
            );
        }

        Set<String> termosEnviadosNames = new HashSet<>(termosNames);
        List<TermsEntity> termosEnviados = termsRepository.findByTermTypeNames(termosEnviadosNames);

        validarTermosVigentes(termosEnviadosNames);

        salvarTermos(userEntity, termosEnviados, UserTermsAction.ACCEPTED);
    }

    public void revogarTermos(List<String> termosNames, AppUserEntity userEntity) {
        validarTermosEnviados(termosNames);

        Set<String> termosEnviadosNames = new HashSet<>(termosNames);
        List<TermsEntity> termosEnviados = termsRepository.findByTermTypeNames(termosEnviadosNames);

        validarTermosVigentes(termosEnviadosNames);

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

    public void registrarCienciaTermos(List<String> termosNames, AppUserEntity userEntity) {
        validarTermosEnviados(termosNames);

        Set<String> termosEnviadosNames = new HashSet<>(termosNames);
        List<TermsEntity> termosEnviados = termsRepository.findByTermTypeNames(termosEnviadosNames);

        validarTermosVigentes(termosEnviadosNames);

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

    private void validarTermosEnviados(List<String> termosNames) {
        if (termosNames == null || termosNames.isEmpty()) {
            throw new NenhumTermoPassadoException("Lista de termos nao pode ser vazia.");
        }

        Set<String> termosEnviadosNames = new HashSet<>(termosNames);
        List<TermsEntity> termosEnviados = termsRepository.findByTermTypeNames(termosEnviadosNames);

        if (termosEnviados.size() != termosEnviadosNames.size()) {
            throw new TermoNaoEncontradoException(
                "Um ou mais termos informados nao existem."
            );
        }
    }

    private void validarTermosVigentes(Set<String> termosEnviadosNames) {
        Set<String> namesVigentes = termsRepository
            .findActiveByReferenceTime(LocalDateTime.now())
            .stream()
            .collect(Collectors.toMap(
                t -> t.getTermType().getName(),
                t -> t,
                (existente, novo) -> existente.getVersion() > novo.getVersion() ? existente : novo
            ))
            .values()
            .stream()
            .map(term -> term.getTermType().getName())
            .collect(Collectors.toSet());

        for (String nameEnviado : termosEnviadosNames) {
            if (!namesVigentes.contains(nameEnviado)) {
                throw new TermoNaoEncontradoException(
                    "Termo enviado nao é vigente: " + nameEnviado
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

    public Boolean checkRequiredTerms(List<String> termosNames, AppUserEntity user, LocalDateTime referenceTime) {
        // return true;

        Set<String> aceitos = (termosNames == null)
            ? new HashSet<>()
            : new HashSet<>(termosNames);

        List<String> obrigatoriosNames = termsRepository
            .findActiveRequiredByReferenceTime(referenceTime)
            .stream()
            .map(term -> term.getTermType().getName())
            .toList();

        if (user != null) {
            List<UserTermsEntity> activeUserTerms = findActiveUserTermsAtTime (
                user,
                referenceTime
            );
            for (UserTermsEntity userTerm : activeUserTerms) {
                String termName = userTerm.getTerms().getTermType().getName();

                if (obrigatoriosNames.contains(termName)) {
                    aceitos.add(termName);
                }
            }
        }

        boolean isMissing = obrigatoriosNames.stream()
            .anyMatch(name -> !aceitos.contains(name));

        // throw new Error(
        //     "| enviados: " + aceitos + " | obrigatorios: " + obrigatoriosNames
        // );

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
