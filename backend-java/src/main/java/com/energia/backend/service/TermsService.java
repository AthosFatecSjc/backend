package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

    private TermsRepository termsRepository;
    private UserTermsRespository userTermsRespository;

    public TermsService(TermsRepository termsRepository, UserTermsRespository userTermsRespository){
        this.termsRepository = termsRepository;
        this.userTermsRespository = userTermsRespository;
    }

    public void registrarTermosAceitos(List<UUID> termosIds, AppUserEntity userEntity) {

        if (termosIds == null || termosIds.isEmpty()) {
            throw new TermoNaoEncontradoException("Lista de termos não pode ser vazia.");
        }

        List<TermsEntity> termos = termsRepository.findAllById(termosIds);

        if (termos.size() != termosIds.size()) {
            throw new TermoNaoEncontradoException("Um ou mais termos informados não existem.");
        }

        try {
            for (TermsEntity termo : termos) {

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
