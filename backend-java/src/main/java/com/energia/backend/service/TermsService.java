package com.energia.backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.energia.backend.model.AppUserEntity;
import com.energia.backend.model.TermsEntity;
import com.energia.backend.model.UserTermsEntity;
import com.energia.backend.model.Usuario;
import com.energia.backend.repository.TermsRepository;
import com.energia.backend.repository.UserTermsRespository;
import com.energia.backend.repository.UsuarioRepository;

@Service
public class TermsService {

    private final UserTermsRespository userTermsRespository;
    private final TermsRepository termsRepository;
    private final UsuarioRepository userRepository;

    public TermsService(UserTermsRespository userTermsRespository,
                        TermsRepository termsRepository,
                    UsuarioRepository userRepository) {
        this.userTermsRespository = userTermsRespository;
        this.termsRepository = termsRepository;
        this.userRepository = userRepository;
    }

    public void registrarTermosAceitos(List<UUID> termosIds, Usuario usuario) {
        AppUserEntity userEntity = userRepository
                .findByEmail(usuario.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
                List<TermsEntity> termos = termsRepository.findAllById(termosIds);

        for (TermsEntity termo : termos) {

            UserTermsEntity userTerms = new UserTermsEntity();
            userTerms.setUser(userEntity);
            userTerms.setTerms(termo);
            userTerms.setAcceptedAt(LocalDateTime.now());
            userTerms.setAcceptedFromIp("127.0.0.1"); // depois você pode pegar do request

            userTermsRespository.save(userTerms);
        }
    }
}
