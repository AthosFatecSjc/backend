package com.energia.backend.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.energia.backend.dto.BulkEmailResponse;
import com.energia.backend.model.privacy.ExternalUserPrivacyRecord;
import com.energia.backend.repository.ExternalUserPrivacyRecordRepository;

@Service
public class BulkUserEmailService {

    private static final Logger logger = LoggerFactory.getLogger(BulkUserEmailService.class);

    private final ExternalUserPrivacyRecordRepository privacyRecordRepository;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public BulkUserEmailService(
            ExternalUserPrivacyRecordRepository privacyRecordRepository,
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@energia.local}") String fromAddress
    ) {
        this.privacyRecordRepository = privacyRecordRepository;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public BulkEmailResponse sendToAllNonDeletedUsers(String subject, String body) {
        String normalizedSubject = requireText(subject, "Assunto e obrigatorio.");
        String normalizedBody = requireText(body, "Mensagem e obrigatoria.");

        List<ExternalUserPrivacyRecord> activeRecords =
                privacyRecordRepository.findAllByDeletedAtIsNullAndEmailIsNotNull();

        Set<String> recipients = new LinkedHashSet<>();
        for (ExternalUserPrivacyRecord record : activeRecords) {
            if (record.getEmail() != null && !record.getEmail().trim().isEmpty()) {
                recipients.add(record.getEmail().trim().toLowerCase());
            }
        }

        int sentCount = 0;
        int failedCount = 0;
        for (String recipient : recipients) {
            try {
                mailSender.send(buildMessage(recipient, normalizedSubject, normalizedBody));
                sentCount++;
            } catch (MailException ex) {
                failedCount++;
                logger.warn("Falha ao enviar comunicado LGPD para um destinatario. erro={}", ex.getMessage());
            }
        }

        String resultMessage = failedCount == 0
                ? "Envio concluido para todos os usuarios nao deletados."
                : "Envio concluido com falhas para parte dos destinatarios.";

        return new BulkEmailResponse(recipients.size(), sentCount, failedCount, resultMessage);
    }

    private SimpleMailMessage buildMessage(String recipient, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(recipient);
        message.setSubject(subject);
        message.setText(body);
        return message;
    }

    private String requireText(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
