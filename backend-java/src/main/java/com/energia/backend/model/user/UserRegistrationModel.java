package com.energia.backend.model.user;

import com.energia.backend.model.StatusUsuario;
import com.energia.backend.model.term.AcceptedTermModel;

import java.util.List;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class UserRegistrationModel {
    private AppUserModel user;
    private List<AcceptedTermModel> acceptedTerms;
}
