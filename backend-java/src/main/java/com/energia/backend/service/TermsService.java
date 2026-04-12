package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.energia.backend.dto.HistoricoTermoResponse;
import com.energia.backend.dto.TermosPendentesResponse;
import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.term.AcceptedTermModel;
import com.energia.backend.model.UserTermsEventType;
import com.energia.backend.model.UserTermsEntity;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRespository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class TermsService {
    private final TermsRepository termsRepository;
    private final UserTermsRespository userTermsRespository;

    public TermsService(TermsRepository termsRepository, UserTermsRespository userTermsRespository) {
        this.termsRepository = termsRepository;
        this.userTermsRespository = userTermsRespository;
    }

    public void registrarTermosAceitos(List<AcceptedTermModel> acceptedTerms, AppUserEntity userEntity) {
        registrarTermosAceitos(acceptedTerms.stream().map(AcceptedTermModel::getId).toList(), userEntity, "127.0.0.1");
    }

    public void registrarTermosAceitos(List<UUID> termosIds, AppUserEntity userEntity, String ipOrigem) {
        if (termosIds == null || termosIds.isEmpty()) {
            throw new TermoNaoEncontradoException("Lista de termos nao pode ser vazia.");
        }

        List<TermsEntity> termosEnviados = termsRepository.findAllWithTypeByIdIn(termosIds);

        if (termosEnviados.size() != termosIds.size()) {
            throw new TermoNaoEncontradoException("Um ou mais termos informados nao existem.");
        }

        Map<String, TermsEntity> termosObrigatoriosVigentesPorTipo = termsRepository
                .findActiveRequiredByReferenceTime(LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(
                        t -> t.getTermType().getName(),
                        t -> t,
                        (existente, novo) -> existente.getVersion() > novo.getVersion() ? existente : novo));

        Set<UUID> termosEnviadosIds = new HashSet<>(termosIds);

        for (TermsEntity termoObrigatorio : termosObrigatoriosVigentesPorTipo.values()) {
            if (!termosEnviadosIds.contains(termoObrigatorio.getId())) {
                throw new TermoNaoEncontradoException(
                        "Termo obrigatorio vigente nao aceito: " + termoObrigatorio.getTermType().getName());
            }
        }

        try {
            List<UserTermsEntity> historico = userTermsRespository.findHistoryByUserId(userEntity.getId());
            Map<UUID, UserTermsEntity> ultimaDecisaoPorTermo = historico.stream()
                    .collect(Collectors.toMap(
                            item -> item.getTerms().getId(),
                            item -> item,
                            (existente, novo) -> compareByActionTime(existente, novo) >= 0 ? existente : novo
                    ));

            LocalDateTime agora = LocalDateTime.now();
            String ipNormalizado = normalizarIp(ipOrigem);

            for (TermsEntity termo : termosEnviados) {
                UserTermsEntity ultimoEvento = ultimaDecisaoPorTermo.get(termo.getId());
                if (ultimoEvento != null && ultimoEvento.getEventType() == UserTermsEventType.ACCEPTED) {
                    continue;
                }

                userTermsRespository.save(buildAcceptedEvent(userEntity, termo, agora, ipNormalizado));
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao registrar aceite dos termos.", ex);
        }
    }

    public List<HistoricoTermoResponse> listarHistorico(UUID userId) {
        return userTermsRespository.findHistoryByUserId(userId).stream()
                .map(this::toHistoricoResponse)
                .toList();
    }

    public List<TermosPendentesResponse> listarPendenciasDeAcesso(UUID userId) {
        return construirPendencias(userId, carregarTermosVigentesPorTipo(false), false);
    }

    public List<TermosPendentesResponse> listarPendenciasObrigatorias(UUID userId) {
        return construirPendencias(userId, carregarTermosVigentesPorTipo(true), true);
    }

    public boolean hasPendingRequiredTerms(UUID userId) {
        return !listarPendenciasObrigatorias(userId).isEmpty();
    }

    public boolean hasPendingTermsForAccess(UUID userId) {
        return !listarPendenciasDeAcesso(userId).isEmpty();
    }

    public void registrarDecisoesPendentes(
            AppUserEntity userEntity,
            List<UUID> requiredTermsIds,
            List<UUID> optionalAcceptedTermsIds,
            String ipOrigem
    ) {
        Map<String, TermsEntity> vigentesPorTipo = carregarTermosVigentesPorTipo(false);

        Set<UUID> requiredInformados = requiredTermsIds == null ? Set.of() : new HashSet<>(requiredTermsIds);
        Set<UUID> optionalAceitos = optionalAcceptedTermsIds == null ? Set.of() : new HashSet<>(optionalAcceptedTermsIds);

        Set<UUID> requiredVigentes = vigentesPorTipo.values().stream()
                .filter(termo -> Boolean.TRUE.equals(termo.getIsRequired()))
                .map(TermsEntity::getId)
                .collect(Collectors.toSet());
        Set<UUID> optionalVigentes = vigentesPorTipo.values().stream()
                .filter(termo -> !Boolean.TRUE.equals(termo.getIsRequired()))
                .map(TermsEntity::getId)
                .collect(Collectors.toSet());

        if (!requiredInformados.equals(requiredVigentes)) {
            throw new TermoNaoEncontradoException("Todos os termos obrigatorios vigentes devem ser aceitos.");
        }

        if (!optionalVigentes.containsAll(optionalAceitos)) {
            throw new TermoNaoEncontradoException("Os termos opcionais informados nao correspondem aos vigentes.");
        }

        String ipNormalizado = normalizarIp(ipOrigem);
        LocalDateTime agora = LocalDateTime.now();
        Map<String, UserTermsEntity> ultimaDecisaoPorTipo = carregarUltimaDecisaoPorTipo(userEntity.getId());

        for (TermsEntity termoVigente : vigentesPorTipo.values()) {
            UserTermsEventType eventoDesejado = Boolean.TRUE.equals(termoVigente.getIsRequired()) || optionalAceitos.contains(termoVigente.getId())
                    ? UserTermsEventType.ACCEPTED
                    : UserTermsEventType.REVOKED;

            UserTermsEntity ultimoEvento = ultimaDecisaoPorTipo.get(termoVigente.getTermType().getName());
            boolean jaRegistradoNaVersaoAtual = ultimoEvento != null
                    && Objects.equals(ultimoEvento.getTerms().getId(), termoVigente.getId())
                    && ultimoEvento.getEventType() == eventoDesejado;

            if (jaRegistradoNaVersaoAtual) {
                continue;
            }

            UserTermsEntity evento = eventoDesejado == UserTermsEventType.ACCEPTED
                    ? buildAcceptedEvent(userEntity, termoVigente, agora, ipNormalizado)
                    : buildRevokedEvent(userEntity, termoVigente, agora, ipNormalizado);
            userTermsRespository.save(evento);
        }
    }

    public void revogarConsentimentoOpcional(AppUserEntity userEntity, UUID termId, String ipOrigem) {
        TermsEntity termo = termsRepository.findAllWithTypeByIdIn(List.of(termId)).stream()
                .findFirst()
                .orElseThrow(() -> new TermoNaoEncontradoException("Termo informado nao existe."));

        if (Boolean.TRUE.equals(termo.getIsRequired())) {
            throw new IllegalArgumentException("Somente consentimentos opcionais podem ser revogados.");
        }

        Map<String, TermsEntity> vigentesPorTipo = carregarTermosVigentesPorTipo(false);
        TermsEntity termoVigenteDoTipo = vigentesPorTipo.get(termo.getTermType().getName());
        if (termoVigenteDoTipo == null || !Objects.equals(termoVigenteDoTipo.getId(), termo.getId())) {
            throw new IllegalArgumentException("Somente o termo opcional vigente pode ser alterado.");
        }

        UserTermsEntity ultimoEvento = userTermsRespository.findHistoryByUserId(userEntity.getId()).stream()
                .filter(item -> item.getTerms().getTermType().getName().equals(termo.getTermType().getName()))
                .findFirst()
                .orElse(null);

        if (ultimoEvento != null
                && Objects.equals(ultimoEvento.getTerms().getId(), termo.getId())
                && ultimoEvento.getEventType() == UserTermsEventType.REVOKED) {
            return;
        }

        LocalDateTime agora = LocalDateTime.now();
        UserTermsEntity revogacao = buildRevokedEvent(userEntity, termo, agora, normalizarIp(ipOrigem));
        userTermsRespository.save(revogacao);
    }

    private Map<String, UserTermsEntity> carregarUltimaDecisaoPorTipo(UUID userId) {
        return userTermsRespository.findHistoryByUserId(userId).stream()
                .collect(Collectors.toMap(
                        item -> item.getTerms().getTermType().getName(),
                        item -> item,
                        (existente, novo) -> compareByActionTime(existente, novo) >= 0 ? existente : novo
                ));
    }

    private Map<String, TermsEntity> carregarTermosVigentesPorTipo(boolean apenasObrigatorios) {
        List<TermsEntity> termos = apenasObrigatorios
                ? termsRepository.findActiveRequiredByReferenceTime(LocalDateTime.now())
                : termsRepository.findActiveByReferenceTime(LocalDateTime.now());

        return termos.stream()
                .collect(Collectors.toMap(
                        t -> t.getTermType().getName(),
                        t -> t,
                        this::selectHigherVersion
                ));
    }

    private List<TermosPendentesResponse> construirPendencias(
            UUID userId,
            Map<String, TermsEntity> vigentesPorTipo,
            boolean apenasAceiteObrigatorio
    ) {
        Map<String, UserTermsEntity> ultimaDecisaoPorTipo = carregarUltimaDecisaoPorTipo(userId);
        List<TermosPendentesResponse> pendencias = new ArrayList<>();

        for (TermsEntity termoVigente : vigentesPorTipo.values()) {
            UserTermsEntity ultimoEvento = ultimaDecisaoPorTipo.get(termoVigente.getTermType().getName());
            boolean versaoAtualFoiDecidida = ultimoEvento != null
                    && Objects.equals(ultimoEvento.getTerms().getId(), termoVigente.getId())
                    && (ultimoEvento.getEventType() == UserTermsEventType.ACCEPTED
                    || (!apenasAceiteObrigatorio
                    && !Boolean.TRUE.equals(termoVigente.getIsRequired())
                    && ultimoEvento.getEventType() == UserTermsEventType.REVOKED));

            if (!versaoAtualFoiDecidida) {
                pendencias.add(new TermosPendentesResponse(
                        termoVigente.getId(),
                        termoVigente.getTermType().getName(),
                        termoVigente.getVersion(),
                        Boolean.TRUE.equals(termoVigente.getIsRequired())
                ));
            }
        }

        return pendencias.stream()
                .sorted(Comparator.comparing(TermosPendentesResponse::type))
                .toList();
    }

    private UserTermsEntity buildAcceptedEvent(
            AppUserEntity userEntity,
            TermsEntity termo,
            LocalDateTime actionAt,
            String ipOrigem
    ) {
        return UserTermsEntity.builder()
                .user(userEntity)
                .terms(termo)
                .acceptedAt(actionAt)
                .acceptedFromIp(ipOrigem)
                .eventType(UserTermsEventType.ACCEPTED)
                .build();
    }

    private UserTermsEntity buildRevokedEvent(
            AppUserEntity userEntity,
            TermsEntity termo,
            LocalDateTime actionAt,
            String ipOrigem
    ) {
        return UserTermsEntity.builder()
                .user(userEntity)
                .terms(termo)
                .acceptedAt(actionAt)
                .acceptedFromIp(ipOrigem)
                .eventType(UserTermsEventType.REVOKED)
                .revokedAt(actionAt)
                .build();
    }

    private HistoricoTermoResponse toHistoricoResponse(UserTermsEntity item) {
        LocalDateTime actionAt = resolveActionTime(item);
        return new HistoricoTermoResponse(
                item.getId(),
                item.getTerms().getId(),
                item.getTerms().getTermType().getName(),
                item.getTerms().getVersion(),
                Boolean.TRUE.equals(item.getTerms().getIsRequired()),
                item.getEventType().name(),
                actionAt,
                item.getAcceptedFromIp()
        );
    }

    private int compareByActionTime(UserTermsEntity left, UserTermsEntity right) {
        return resolveActionTime(left).compareTo(resolveActionTime(right));
    }

    private LocalDateTime resolveActionTime(UserTermsEntity item) {
        return item.getEventType() == UserTermsEventType.REVOKED && item.getRevokedAt() != null
                ? item.getRevokedAt()
                : item.getAcceptedAt();
    }

    private TermsEntity selectHigherVersion(TermsEntity existente, TermsEntity novo) {
        return existente.getVersion() > novo.getVersion() ? existente : novo;
    }

    private String normalizarIp(String ipOrigem) {
        if (ipOrigem == null || ipOrigem.trim().isEmpty()) {
            return "127.0.0.1";
        }
        return ipOrigem.trim();
    }
}
