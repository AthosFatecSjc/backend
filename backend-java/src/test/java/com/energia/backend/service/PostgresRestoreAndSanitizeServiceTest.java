package com.energia.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostgresRestoreAndSanitizeService Tests")
class PostgresRestoreAndSanitizeServiceTest {

    @Mock
    private ExternalUserPrivacyRegistryService privacyRegistryService;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private PostgresRestoreAndSanitizeService service;

    private UUID testUserId1;
    private UUID testUserId2;

    @BeforeEach
    void setup() {
        testUserId1 = UUID.randomUUID();
        testUserId2 = UUID.randomUUID();

        ReflectionTestUtils.setField(service, "backupDirectory", "./backups");
        ReflectionTestUtils.setField(service, "postgresContainerName", "energia-postgres");
        ReflectionTestUtils.setField(service, "postgresUsername", "energia_user");
        ReflectionTestUtils.setField(service, "datasourceUrl",
            "jdbc:postgresql://localhost:5432/energia_db");
    }

    @Test
    @DisplayName("Service deve estar instanciado corretamente")
    void testServiceIsInstantiated() {
        assertNotNull(service);
    }

    @Test
    @DisplayName("Deve lançar exceção quando não há backup no diretório")
    void testThrowsExceptionWhenNoBackup() {
        ReflectionTestUtils.setField(service, "backupDirectory", "/tmp/nonexistent-dir-xyz");

        Exception exception = assertThrows(Exception.class, () -> {
            service.restoreFromLatestBackupAndSanitize();
        });

        assertNotNull(exception.getMessage());
    }

    @Test
    @DisplayName("Deve consultar registros de privacidade na sanitização")
    void testSanitizeQueriesPrivacyRegistry() {
        Set<UUID> deletedUserIds = Set.of(testUserId1, testUserId2);
        when(privacyRegistryService.findAllDeletedUserIds()).thenReturn(deletedUserIds);

        ReflectionTestUtils.invokeMethod(service, "sanitizeDeletedUsers");

        verify(privacyRegistryService, times(1)).findAllDeletedUserIds();
    }

    @Test
    @DisplayName("Não deve chamar jdbcTemplate quando não há usuários deletados")
    void testNoUsersToSanitize() {
        when(privacyRegistryService.findAllDeletedUserIds()).thenReturn(Set.of());

        ReflectionTestUtils.invokeMethod(service, "sanitizeDeletedUsers");

        verify(privacyRegistryService, times(1)).findAllDeletedUserIds();
        verifyNoInteractions(jdbcTemplate);
    }
}
