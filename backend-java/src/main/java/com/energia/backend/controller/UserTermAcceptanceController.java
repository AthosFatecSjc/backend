package com.energia.backend.controller;
import com.energia.backend.DTO.UserTermAcceptanceRequestDTO;
import com.energia.backend.DTO.UserTermAcceptanceResponseDTO;
import com.energia.backend.DTO.UserTermAcceptanceSignupRequestDTO;
import com.energia.backend.service.UserTermAcceptanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/user-terms")
public class UserTermAcceptanceController {

    private final UserTermAcceptanceService service;

    public UserTermAcceptanceController(UserTermAcceptanceService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<UserTermAcceptanceResponseDTO> acceptTerm(
            @Valid @RequestBody UserTermAcceptanceRequestDTO dto
    ) {
        UserTermAcceptanceResponseDTO response = service.acceptTerm(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup-and-accept")
    public ResponseEntity<UserTermAcceptanceResponseDTO> signupAndAcceptMandatoryTerm(
            @Valid @RequestBody UserTermAcceptanceSignupRequestDTO dto
    ) {
        UserTermAcceptanceRequestDTO core = new UserTermAcceptanceRequestDTO();
        core.setTermId(dto.getTermId());
        core.setAccepted(dto.getAccepted());
        core.setName(dto.getName());
        core.setEmail(dto.getEmail());
        core.setPassword(dto.getPassword());
        core.setPhone(dto.getPhone());

        UserTermAcceptanceResponseDTO response = service.acceptTerm(core);
        return ResponseEntity.ok(response);
    }
}