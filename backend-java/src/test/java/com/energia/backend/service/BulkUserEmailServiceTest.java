package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.energia.backend.dto.BulkEmailResponse;
import com.energia.backend.model.privacy.ExternalUserPrivacyRecord;
import com.energia.backend.repository.ExternalUserPrivacyRecordRepository;

class BulkUserEmailServiceTest {

    @Test
    void deveEnviarEmailSomenteParaUsuariosNaoDeletadosDoRegistroExterno() {
        ExternalUserPrivacyRecordRepository repository = mock(ExternalUserPrivacyRecordRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        BulkUserEmailService service = new BulkUserEmailService(
                repository,
                mailSender,
                "incidentes@energia.local"
        );

        when(repository.findAllByDeletedAtIsNullAndEmailIsNotNull()).thenReturn(List.of(
                ExternalUserPrivacyRecord.builder()
                        .userId(UUID.randomUUID())
                        .email("USER1@TESTE.COM")
                        .deletedAt(null)
                        .build(),
                ExternalUserPrivacyRecord.builder()
                        .userId(UUID.randomUUID())
                        .email("user2@teste.com")
                        .deletedAt(null)
                        .build()
        ));

        BulkEmailResponse response = service.sendToAllNonDeletedUsers(
                "Comunicado importante",
                "Mensagem do incidente."
        );

        assertEquals(2, response.recipientsFound());
        assertEquals(2, response.sentCount());
        assertEquals(0, response.failedCount());

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, org.mockito.Mockito.times(2)).send(captor.capture());

        List<SimpleMailMessage> messages = captor.getAllValues();
        assertEquals("incidentes@energia.local", messages.get(0).getFrom());
        assertEquals("user1@teste.com", messages.get(0).getTo()[0]);
        assertEquals("user2@teste.com", messages.get(1).getTo()[0]);
    }

    @Test
    void deveDeduplicarDestinatariosEContinuarQuandoUmEnvioFalhar() {
        ExternalUserPrivacyRecordRepository repository = mock(ExternalUserPrivacyRecordRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        BulkUserEmailService service = new BulkUserEmailService(
                repository,
                mailSender,
                "incidentes@energia.local"
        );

        when(repository.findAllByDeletedAtIsNullAndEmailIsNotNull()).thenReturn(List.of(
                ExternalUserPrivacyRecord.builder()
                        .userId(UUID.randomUUID())
                        .email("user1@teste.com")
                        .deletedAt(null)
                        .build(),
                ExternalUserPrivacyRecord.builder()
                        .userId(UUID.randomUUID())
                        .email(" USER1@TESTE.COM ")
                        .deletedAt(null)
                        .build(),
                ExternalUserPrivacyRecord.builder()
                        .userId(UUID.randomUUID())
                        .email("user2@teste.com")
                        .deletedAt(null)
                        .build()
        ));
        doThrow(new MailSendException("smtp offline"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        BulkEmailResponse response = service.sendToAllNonDeletedUsers(
                "Comunicado",
                "Corpo"
        );

        assertEquals(2, response.recipientsFound());
        assertEquals(0, response.sentCount());
        assertEquals(2, response.failedCount());
        verify(mailSender, org.mockito.Mockito.times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void deveRejeitarAssuntoOuMensagemVazios() {
        ExternalUserPrivacyRecordRepository repository = mock(ExternalUserPrivacyRecordRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        BulkUserEmailService service = new BulkUserEmailService(
                repository,
                mailSender,
                "incidentes@energia.local"
        );

        IllegalArgumentException subjectException = assertThrows(
                IllegalArgumentException.class,
                () -> service.sendToAllNonDeletedUsers(" ", "Mensagem")
        );
        IllegalArgumentException bodyException = assertThrows(
                IllegalArgumentException.class,
                () -> service.sendToAllNonDeletedUsers("Assunto", " ")
        );

        assertEquals("Assunto e obrigatorio.", subjectException.getMessage());
        assertEquals("Mensagem e obrigatoria.", bodyException.getMessage());
    }

    @Test
    void naoDeveConsultarBancoPrincipalParaMontarDestinatarios() {
        ExternalUserPrivacyRecordRepository repository = mock(ExternalUserPrivacyRecordRepository.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        BulkUserEmailService service = new BulkUserEmailService(
                repository,
                mailSender,
                "incidentes@energia.local"
        );

        when(repository.findAllByDeletedAtIsNullAndEmailIsNotNull()).thenReturn(List.of(
                ExternalUserPrivacyRecord.builder()
                        .userId(UUID.randomUUID())
                        .email("ativo@teste.com")
                        .deletedAt(null)
                        .createdAt(LocalDateTime.now())
                        .build()
        ));

        service.sendToAllNonDeletedUsers("Assunto", "Mensagem");

        verify(repository).findAllByDeletedAtIsNullAndEmailIsNotNull();
    }
}
