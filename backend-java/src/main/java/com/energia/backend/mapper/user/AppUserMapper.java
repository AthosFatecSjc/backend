package com.energia.backend.mapper.user;

import org.springframework.stereotype.Component;

import com.energia.backend.dto.UsuarioCadastroRequest;
import com.energia.backend.dto.UsuarioCadastroResponse;
import com.energia.backend.dto.term.AcceptedTermRequestDto;
import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.term.AcceptedTermModel;
import com.energia.backend.model.user.AppUserModel;
import com.energia.backend.model.user.UserRegistrationModel;

import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppUserMapper {

        public UserRegistrationModel fromDto(UsuarioCadastroRequest dto) {
                if (dto == null) return null;

                return UserRegistrationModel.builder()
                        .user(
                                AppUserModel.builder()
                                        .fullName(dto.getNomeCompleto())
                                        .email(dto.getEmail())
                                        .password(dto.getSenha()) //Password hashing is handled in the service layer
                                        .phone(dto.getTelefone())
                                        .status(StatusUsuario.PENDENTE)
                                        .createdAt(LocalDateTime.now())
                                        .build()
                        )
                        .acceptedTerms(mapTerms(dto.getTerms()))
                        .build();
        }

        private List<AcceptedTermModel> mapTerms(List<AcceptedTermRequestDto> terms) {
                if (terms == null) return List.of();

                return terms.stream()
                        .map(term -> AcceptedTermModel.builder()
                                .id(term.getId())
                                .version(term.getVersion())
                                .build()
                        )
                        .collect(Collectors.toList());
        }

        public UsuarioCadastroResponse toDto(AppUserModel user) {
                if (user == null) return null;

                return UsuarioCadastroResponse.builder()
                        .mensagem("Registration completed successfully. Awaiting administrator approval")
                        .email(user.getEmail())
                        .status(user.getStatus())
                        .build();
        }
}