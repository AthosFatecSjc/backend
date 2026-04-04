package com.energia.backend.controller;

import com.energia.backend.dto.AuthenticationErrorResponse;
import com.energia.backend.dto.LoginRequest;
import com.energia.backend.dto.LoginResponse;
import com.energia.backend.exception.LoginAuthenticationException;
import com.energia.backend.service.AuthenticationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authenticationService.authenticate(request);
            return ResponseEntity.ok(response);
        } catch (LoginAuthenticationException ex) {
            AuthenticationErrorResponse errorResponse = new AuthenticationErrorResponse(
                    ex.getHttpStatus(),
                    ex.getErrorCode(),
                    ex.getMessage(),
                    "INFO",
                    ex.getReason()
            );
            return ResponseEntity
                    .status(ex.getHttpStatus())
                    .body(errorResponse);
        } catch (IllegalArgumentException ex) {
            AuthenticationErrorResponse errorResponse = new AuthenticationErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "INVALID_REQUEST",
                    ex.getMessage(),
                    "INFO"
            );
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }
    }
}
