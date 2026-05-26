package com.energia.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.energia.backend.model.DeletionStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PostgresRestoreAndSanitizeService {

    private static final Logger logger = LoggerFactory.getLogger(PostgresRestoreAndSanitizeService.class);

    @Autowired
    private ExternalUserPrivacyRegistryService privacyRegistryService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${backup.directory}")
    private String backupDirectory;

    @Value("${postgres.docker.container:energia-postgres}")
    private String postgresContainerName;

    @Value("${spring.datasource.username}")
    private String postgresUsername;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    private String getDatabaseName() {
        String dbName = datasourceUrl.substring(datasourceUrl.lastIndexOf("/") + 1);
        return dbName.split("\\?")[0];
    }

    public void restoreFromLatestBackupAndSanitize() throws IOException, InterruptedException {
        logger.info("Iniciando restore do backup mais recente e sanitização");

        try {
            Optional<File> latestBackup = findLatestBackup();

            if (!latestBackup.isPresent()) {
                throw new RuntimeException("Nenhum arquivo de backup encontrado em: " + backupDirectory);
            }

            File backupFile = latestBackup.get();
            logger.info("Backup encontrado: {}", backupFile.getAbsolutePath());

            copyBackupToContainer(backupFile);
            executeRestore(backupFile.getName());

            sanitizeDeletedUsers();

            logger.info("Restore e sanitização concluídos com sucesso");
        } catch (Exception e) {
            logger.error("Erro durante restore e sanitização", e);
            throw e;
        }
    }

    private void sanitizeDeletedUsers() {
        logger.info("Iniciando sanitização de usuários deletados");

        Set<UUID> deletedUserIds = privacyRegistryService.findAllDeletedUserIds();

        if (deletedUserIds.isEmpty()) {
            logger.info("Nenhum usuário marcado para exclusão");
            return;
        }

        logger.info("Encontrados {} usuários para sanitizar", deletedUserIds.size());

        for (UUID userId : deletedUserIds) {
            try {
                String sql = "DELETE FROM energia.user_personal_data WHERE user_id = ?";
                int deletedRows = jdbcTemplate.update(sql, userId);

                if (deletedRows > 0) {
                    logger.debug("Deletados {} registros pessoais do usuário: {}", deletedRows, userId);
                }

                String updateSql = """
                        UPDATE energia.app_user
                        SET deletion_status = ?,
                            deleted_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """;

                jdbcTemplate.update(
                        updateSql,
                        DeletionStatus.DELETED.name(),
                        userId);

            } catch (Exception e) {
                logger.error("Erro ao sanitizar dados do usuário {}", userId, e);
            }
        }

        logger.info("Sanitização concluída");
    }

    private Optional<File> findLatestBackup() {
        try {
            return Files.list(Paths.get(backupDirectory))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("backup_"))
                    .map(Path::toFile)
                    .max(Comparator.comparingLong(File::lastModified));
        } catch (IOException e) {
            logger.error("Erro ao listar arquivos de backup", e);
            return Optional.empty();
        }
    }

    private void copyBackupToContainer(File backupFile) throws IOException, InterruptedException {
        String command = String.format(
                "docker cp %s %s:/tmp/%s",
                backupFile.getAbsolutePath(),
                postgresContainerName,
                backupFile.getName());

        logger.info("Copiando backup para container");
        executeCommand(command);
    }

    private void executeRestore(String backupFileName) throws IOException, InterruptedException {
        String databaseName = getDatabaseName();

        truncateAllTables(databaseName);

        String command = String.format(
                "docker exec %s psql -U %s -d %s -f /tmp/%s",
                postgresContainerName,
                postgresUsername,
                databaseName,
                backupFileName);

        logger.info("Executando restore no container");
        executeCommand(command);
    }

    private void truncateAllTables(String databaseName) throws IOException, InterruptedException {
        logger.info("Truncando tabelas dos schemas energia e aneel antes do restore");

        String truncateSql = "DO \\$\\$ DECLARE r RECORD; " +
                "BEGIN " +
                "  FOR r IN (SELECT schemaname, tablename FROM pg_tables " +
                "            WHERE schemaname IN ('energia', 'aneel')) LOOP " +
                "    EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.schemaname) || '.' || quote_ident(r.tablename) || ' RESTART IDENTITY CASCADE'; "
                +
                "  END LOOP; " +
                "END \\$\\$;";

        String command = String.format(
                "docker exec %s psql -U %s -d %s -c \"%s\"",
                postgresContainerName,
                postgresUsername,
                databaseName,
                truncateSql);

        executeCommand(command);
    }

    private void executeCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("sh", "-c", command);
        pb.inheritIO();

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Comando falhou com exit code: " + exitCode);
        }
    }
}
