package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.energia.backend.exception.TermoNaoEncontradoException;
import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
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

    public void registrarTermosAceitos(List<UUID> termosIds, AppUserEntity userEntity) {
        if (termosIds == null || termosIds.isEmpty()) {
            throw new TermoNaoEncontradoException("Lista de termos nao pode ser vazia.");
        }

        List<TermsEntity> termosEnviados = termsRepository.findAllById(termosIds);

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
            for (TermsEntity termo : termosEnviados) {
                UserTermsEntity userTerms = new UserTermsEntity();
                userTerms.setUser(userEntity);
                userTerms.setTerms(termo);
                userTerms.setAcceptedAt(LocalDateTime.now());
                userTerms.setAcceptedFromIp("127.0.0.1");

                userTermsRespository.save(userTerms);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Erro ao registrar aceite dos termos.", ex);
        }
    }
}
