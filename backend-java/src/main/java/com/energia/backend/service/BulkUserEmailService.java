package com.energia.backend.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.energia.backend.dto.BulkEmailResponse;

@Service
public class BulkUserEmailService {

    private static final Logger logger = LoggerFactory.getLogger(BulkUserEmailService.class);

    private final MongoTemplate mongoTemplate;
    private final JavaMailSender mailSender;
    private final String fromAddress;

    public BulkUserEmailService(
            MongoTemplate mongoTemplate,
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@energia.local}") String fromAddress
    ) {
        this.mongoTemplate = mongoTemplate;
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    public BulkEmailResponse sendToAllNonDeletedUsers(String subject, String body) {
        String normalizedSubject = requireText(subject, "Assunto e obrigatorio.");
        String normalizedBody = requireText(body, "Mensagem e obrigatoria.");

        Query query = Query.query(Criteria.where("deletedAt").is(null).and("email").ne(null));
        query.fields().include("email").exclude("_id");

        List<Document> activeRecords = mongoTemplate.find(query, Document.class, "lgpd_user_registry");

        Set<String> recipients = activeRecords.stream()
                .map(record -> record.getString("email"))
                .filter(email -> email != null && !email.trim().isEmpty())
                .map(String::trim)
                .map(String::toLowerCase)
                .collect(Collectors.toCollection(LinkedHashSet::new));

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
