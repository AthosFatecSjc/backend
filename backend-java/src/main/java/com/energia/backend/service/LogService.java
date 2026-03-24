package com.energia.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.energia.backend.dto.LogRequest;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.LogRepository;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Serviço centralizado para registro de eventos e auditoria do sistema.
 * Substitui operações CRUD genéricas por um contrato de registro padronizado.
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    /**
     * Registra um evento de sistema de forma padronizada.
     *
     * @param request DTO contendo os dados do evento
     */
    @Transactional
    public void log(@Valid LogRequest request) {
        try {
            SystemLog systemLog = SystemLog.builder()
                    .actorRef(request.getActor())
                    .sourceType(request.getSourceType())
                    .event(request.getEvent())
                    .result(request.getResult())
                    .logCategory(request.getLogCategory())
                    .description(request.getDescription())
                    .metadata(request.getMetadata())
                    .targetRef(request.getTargetRef())
                    .createdByModule(request.getModule())
                    .build();

            logRepository.save(systemLog);
        } catch (Exception e) {
            log.error("Falha crítica ao persistir log de sistema: {}", e.getMessage());
        }
    }
}