package com.energia.backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import com.energia.backend.dto.BulkEmailResponse;

class BulkUserEmailServiceTest {

    @Test
    void deveEnviarEmailSomenteParaUsuariosNaoDeletadosDoRegistroExterno() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        BulkUserEmailService service = new BulkUserEmailService(
                mongoTemplate,
                mailSender,
                "incidentes@energia.local"
        );

        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("lgpd_user_registry")))
                .thenReturn(List.of(
                        new Document("email", "USER1@TESTE.COM"),
                        new Document("email", "user2@teste.com")
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
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        BulkUserEmailService service = new BulkUserEmailService(
                mongoTemplate,
                mailSender,
                "incidentes@energia.local"
        );

        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("lgpd_user_registry")))
                .thenReturn(List.of(
                        new Document("email", "user1@teste.com"),
                        new Document("email", " USER1@TESTE.COM "),
                        new Document("email", "user2@teste.com")
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
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        BulkUserEmailService service = new BulkUserEmailService(
                mongoTemplate,
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
    void devePesquisarApenasEmailsNoRegistroExterno() {
        MongoTemplate mongoTemplate = mock(MongoTemplate.class);
        JavaMailSender mailSender = mock(JavaMailSender.class);
        BulkUserEmailService service = new BulkUserEmailService(
                mongoTemplate,
                mailSender,
                "incidentes@energia.local"
        );

        when(mongoTemplate.find(any(Query.class), eq(Document.class), eq("lgpd_user_registry")))
                .thenReturn(List.of(new Document("email", "ativo@teste.com")));

        service.sendToAllNonDeletedUsers("Assunto", "Mensagem");

        verify(mongoTemplate).find(any(Query.class), eq(Document.class), eq("lgpd_user_registry"));
    }
}
