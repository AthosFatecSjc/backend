package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.energia.backend.dto.HistoricoTermoResponse;
import com.energia.backend.exception.DocumentosObrigatoriosNaoConfiguradosException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.UserTermsAction;
import com.energia.backend.model.UserTermsEntity;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRepository;
import com.energia.backend.repository.UsuarioRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TermsUserService {

    private final TermsRepository termsRepository;
    private final UserTermsRepository userTermsRespository;
    private final TermsService termsService;
    private final UsuarioRepository userRepository;

    public TermsUserService(TermsRepository termsRepository,
            UserTermsRepository userTermsRespository,
            TermsService termsService,
            UsuarioRepository userRepository) {
        this.termsRepository = termsRepository;
        this.userTermsRespository = userTermsRespository;
        this.termsService = termsService;
        this.userRepository = userRepository;
    }

    // ok
    public void aprovarTermos(List<UUID> termosIds, AppUserEntity userEntity) {

        LocalDateTime agora = LocalDateTime.now();

        termsService.validarTermosEnviados(termosIds);

        if (!checkRequiredTerms(termosIds, userEntity, agora)) {
            throw new DocumentosObrigatoriosNaoConfiguradosException(
                    "Termo obrigatório está pendente de aceite");
        }

        List<TermsEntity> termos = termsRepository.findAllById(termosIds);

        for (TermsEntity termo : termos) {
            checarAcaoRepetida(userEntity, termo.getId(), UserTermsAction.ACCEPTED);
        }

        salvarTermos(userEntity, termos, UserTermsAction.ACCEPTED);
    }

    // ok
    public void revogarTermos(List<UUID> termosIds, AppUserEntity userEntity) {

        termsService.validarTermosEnviados(termosIds);

        List<TermsEntity> termos = termsRepository.findAllById(termosIds);

        for (TermsEntity termo : termos) {
            if (termo.getTermType().getIsRequired()) {
                throw new IllegalStateException(
                        "Não é permitido recusar/revogar termo obrigatório: "
                                + termo.getTermType().getName());
            }
            checarAcaoRepetida(userEntity, termo.getId(), UserTermsAction.REVOKED);
        }

        salvarTermos(userEntity, termos, UserTermsAction.REVOKED);
    }

    // ok
    public void registrarCienciaTermos(List<UUID> termosIds, AppUserEntity userEntity) {

        termsService.validarTermosEnviados(termosIds);

        List<TermsEntity> termos = termsRepository.findAllById(termosIds);

        for (TermsEntity termo : termos) {
            if (termo.getTermType().getIsRequired()) {
                throw new IllegalStateException(
                        "Termo obrigatorio deve ser aceito: "
                                + termo.getTermType().getName());
            }
            checarAcaoRepetida(userEntity, termo.getId(), UserTermsAction.ACKNOWLEDGED);
        }

        salvarTermos(userEntity, termos, UserTermsAction.ACKNOWLEDGED);
    }

    // OK
    private void salvarTermos(
            AppUserEntity userEntity,
            List<TermsEntity> termosEnviados,
            UserTermsAction action) {
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

    // ok
    public Boolean checkRequiredTerms(List<UUID> aceitosIds, AppUserEntity user, LocalDateTime referenceTime) {

        Set<UUID> aceitos = (aceitosIds == null)
                ? new HashSet<>()
                : new HashSet<>(aceitosIds);

        List<TermsEntity> obrigatorios = termsRepository
                .findActiveRequiredByReferenceTime(referenceTime);

        Set<UUID> obrigatoriosIds = obrigatorios.stream()
                .map(TermsEntity::getId)
                .collect(Collectors.toSet());

        if (user != null) {
            List<UserTermsEntity> activeUserTerms = findAcceptedUserTermsAtTime(
                    user,
                    referenceTime);
            for (UserTermsEntity userTerm : activeUserTerms) {
                UUID termId = userTerm.getTerms().getId();

                if (obrigatoriosIds.contains(termId)) {
                    aceitos.add(termId);
                }
            }
        }

        return aceitos.containsAll(obrigatoriosIds);
    }

    // ok
    public List<UserTermsEntity> findAcceptedUserTermsAtTime(
            AppUserEntity user,
            LocalDateTime referenceTime) {

        List<TermsEntity> termosVigentes = termsRepository.findActiveByReferenceTime(referenceTime);

        Set<UUID> termosVigentesIds = termosVigentes.stream()
                .map(TermsEntity::getId)
                .collect(Collectors.toSet());

        List<UserTermsEntity> historico = userTermsRespository
                .findByUserAndActionAtLessThanEqual(user, referenceTime);

        Map<String, UserTermsEntity> ultimaAcaoPorTermo = historico
                .stream()
                .filter(ut -> termosVigentesIds.contains(ut.getTerms().getId()))
                .collect(Collectors.toMap(
                        ut -> buildKey(ut),
                        ut -> ut,
                        (a, b) -> a.getActionAt().isAfter(b.getActionAt()) ? a : b));

        return ultimaAcaoPorTermo.values()
                .stream()
                .filter(ut -> ut.getAction() == UserTermsAction.ACCEPTED)
                .toList();
    }

    // ok
    private void checarAcaoRepetida(AppUserEntity user, UUID termId, UserTermsAction action) {
        userTermsRespository.findTopByUserAndTermsIdOrderByActionAtDesc(user, termId)
                .ifPresent(ut -> {
                    if (ut.getAction() == action) {
                        throw new IllegalStateException("Ação entre usuário e termo já existe: " + action);
                    }
                });
    }

    // ok
    private String buildKey(UserTermsEntity ut) {
        return ut.getTerms().getTermType().getId() + "_" + ut.getTerms().getClause();
    }

    public List<HistoricoTermoResponse> listarHistorico(UUID userId) {
        return userTermsRespository.findHistoryByUserId(userId).stream()
                .map(item -> new HistoricoTermoResponse(
                        item.getId(),
                        item.getTerms().getId(),
                        item.getTerms().getTermType().getName().name(),
                        item.getTerms().getTermType().getIsRequired(),
                        item.getAction().name(),
                        item.getActionAt()))
                .toList();
    }

    // ok
    public List<TermsEntity> listarTermosPendentes(UUID userId, Boolean apenasObrigatorios) {
        AppUserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));

        Set<UUID> termosAceitosIds = findAcceptedUserTermsAtTime(user, LocalDateTime.now())
                .stream()
                .map(ut -> ut.getTerms().getId())
                .collect(Collectors.toSet());

        List<TermsEntity> termosVigentes = (apenasObrigatorios
                ? termsRepository.findActiveRequiredByReferenceTime(LocalDateTime.now())
                : termsRepository.findActiveByReferenceTime(LocalDateTime.now()));

        return termosVigentes.stream()
                .filter(t -> !termosAceitosIds.contains(t.getId()))
                .toList();

    }

    // public List<TermosResponse> listarPendenciasObrigatorias(UUID userId) {
    // return construirPendencias(userId, carregarTermosVigentesPorTipo(true),
    // true);
    // }

    // public boolean hasPendingRequiredTerms(UUID userId) {
    // return !listarPendenciasObrigatorias(userId).isEmpty();
    // }

    // public boolean hasPendingTermsForAccess(UUID userId) {
    // return !listarPendenciasDeAcesso(userId).isEmpty();
    // }

    // private List<TermosPendentesResponse> construirPendencias(
    // UUID userId,
    // boolean apenasAceiteObrigatorio) {

    // return List.of();

    // }
}