package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.energia.backend.dto.TermoPeriodoResponse;
import com.energia.backend.dto.TermosResponse;
import com.energia.backend.dto.UserTermResponse;
import com.energia.backend.dto.UsuarioHistoricoTermosResponse;
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

        List<TermsEntity> termosParaSalvar = termos.stream()
                .filter(termo -> !isAcaoRepetida(userEntity, termo.getId(), UserTermsAction.ACCEPTED))
                .toList();

        salvarTermos(userEntity, termosParaSalvar, UserTermsAction.ACCEPTED);
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
        }

        List<TermsEntity> termosParaSalvar = termos.stream()
                .filter(termo -> !isAcaoRepetida(userEntity, termo.getId(), UserTermsAction.REVOKED))
                .toList();

        salvarTermos(userEntity, termosParaSalvar, UserTermsAction.REVOKED);
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
        }
        List<TermsEntity> termosParaCiencia = termos.stream()
                .filter(termo -> !isAcaoRepetida(userEntity, termo.getId(), UserTermsAction.REVOKED))
                .toList();


        salvarTermos(userEntity, termosParaCiencia, UserTermsAction.ACKNOWLEDGED);
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
            List<UserTermResponse> activeUserTerms = findAcceptedUserTermsAtTime(
                    user,
                    referenceTime);
            for (UserTermResponse userTerm : activeUserTerms) {
                UUID termId = userTerm.termId();

                if (obrigatoriosIds.contains(termId)) {
                    aceitos.add(termId);
                }
            }
        }

        return aceitos.containsAll(obrigatoriosIds);
    }

    // ok
    public List<UserTermResponse> findAcceptedUserTermsAtTime(
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
                .map(ut -> new UserTermResponse(
                        ut.getId(),
                        ut.getTerms().getId(),
                        ut.getTerms().getTermType().getName().name(),
                        ut.getTerms().getTermType().getIsRequired(),
                        ut.getAction().name(),
                        ut.getActionAt()))
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

    private boolean isAcaoRepetida(AppUserEntity user, UUID termId, UserTermsAction action) {
        return userTermsRespository.findTopByUserAndTermsIdOrderByActionAtDesc(user, termId)
                .map(lastAction -> lastAction.getAction() == action)
                .orElse(false);
    }

    // ok
    private String buildKey(UserTermsEntity ut) {
        return ut.getTerms().getTermType().getId() + "_" + ut.getTerms().getClause();
    }

    public List<UserTermResponse> listarHistorico(UUID userId) {
        return userTermsRespository.findHistoryByUserId(userId).stream()
                .map(item -> new UserTermResponse(
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
        if (!userRepository.findById(userId).isPresent()) {
            throw new RuntimeException("User not found: " + userId);
        }

        return termsRepository.findPendingLatestTermsByUser(
                userId,
                LocalDateTime.now()).stream()
                .filter(terms -> !apenasObrigatorios || terms.getTermType().getIsRequired())
                .toList();
    }

    public TermosResponse convertToTermosResponse(
            UserTermResponse response) {
        TermsEntity term = termsRepository.findById(response.termId())
                .orElseThrow(() -> new RuntimeException("Termo não encontrado"));

        return new TermosResponse(
                response.termId(),
                response.typeName(),
                response.required(),
                term.getContent(),
                term.getClause(),
                term.getEffectivityStartAt(),
                term.getEffectivityEndAt());
    }

    public UsuarioHistoricoTermosResponse listarHistoricoFormatado(UUID userId) {

        List<UserTermsEntity> history = userTermsRespository.findHistoryByUserId(userId);

        List<TermoPeriodoResponse> termos = new ArrayList<>();

        Map<UUID, List<UserTermsEntity>> grouped = history.stream()
                .collect(Collectors.groupingBy(
                        item -> item.getTerms().getId()));

        for (Map.Entry<UUID, List<UserTermsEntity>> entry : grouped.entrySet()) {

            List<UserTermsEntity> actions = entry.getValue()
                    .stream()
                    .sorted(Comparator.comparing(UserTermsEntity::getActionAt))
                    .toList();

            for (int i = 0; i < actions.size(); i++) {

                UserTermsEntity current = actions.get(i);

                if (!current.getAction().name().equals("ACCEPTED")) {
                    continue;
                }

                LocalDateTime inicio = current.getActionAt();

                LocalDateTime fim = current.getTerms().getEffectivityEndAt();

                for (int j = i + 1; j < actions.size(); j++) {

                    UserTermsEntity next = actions.get(j);

                    if (next.getAction().name().equals("REVOKED")) {
                        fim = minDate(
                                next.getActionAt(),
                                current.getTerms().getEffectivityEndAt());
                        break;
                    }
                }

                termos.add(new TermoPeriodoResponse(
                        current.getTerms().getId(),
                        inicio,
                        fim));
            }
        }

        return new UsuarioHistoricoTermosResponse(
                userId,
                termos);
    }

    public List<UsuarioHistoricoTermosResponse> listarHistoricoFormatadoAllUsers() {

        List<UserTermsEntity> history = userTermsRespository.findHistoryAllUsers();

        Map<UUID, List<UserTermsEntity>> groupedByUser = history.stream()
                .collect(Collectors.groupingBy(item -> item.getUser().getId()));

        List<UsuarioHistoricoTermosResponse> resposta = new ArrayList<>();

        for (Map.Entry<UUID, List<UserTermsEntity>> userEntry : groupedByUser.entrySet()) {

            UUID userId = userEntry.getKey();
            List<UserTermsEntity> userHistory = userEntry.getValue();

            Map<UUID, List<UserTermsEntity>> groupedByTerm = userHistory.stream()
                    .collect(Collectors.groupingBy(item -> item.getTerms().getId()));

            List<TermoPeriodoResponse> termos = new ArrayList<>();

            for (Map.Entry<UUID, List<UserTermsEntity>> termEntry : groupedByTerm.entrySet()) {

                List<UserTermsEntity> actions = termEntry.getValue()
                        .stream()
                        .sorted(Comparator.comparing(UserTermsEntity::getActionAt))
                        .toList();

                for (int i = 0; i < actions.size(); i++) {

                    UserTermsEntity current = actions.get(i);

                    if (!current.getAction().name().equals("ACCEPTED")) {
                        continue;
                    }

                    LocalDateTime inicio = current.getActionAt();
                    LocalDateTime fim = current.getTerms().getEffectivityEndAt();

                    for (int j = i + 1; j < actions.size(); j++) {
                        UserTermsEntity next = actions.get(j);

                        if (next.getAction().name().equals("REVOKED")) {
                            fim = minDate(
                                    next.getActionAt(),
                                    current.getTerms().getEffectivityEndAt());
                            break;
                        }
                    }

                    termos.add(new TermoPeriodoResponse(
                            current.getTerms().getId(),
                            inicio,
                            fim));
                }
            }

            resposta.add(new UsuarioHistoricoTermosResponse(userId, termos));
        }

        return resposta;
    }

    private LocalDateTime minDate(
            LocalDateTime a,
            LocalDateTime b) {
        if (a == null)
            return b;
        if (b == null)
            return a;

        return a.isBefore(b) ? a : b;
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