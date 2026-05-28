package com.energia.backend.runner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.energia.backend.service.PostgresRestoreAndSanitizeService;

@Component
public class BackupRestoreRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(BackupRestoreRunner.class);

    private static final String RESTORE_FLAG = "restore-backup";

    @Autowired
    private PostgresRestoreAndSanitizeService restoreService;

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        if (!args.containsOption(RESTORE_FLAG)) {
            return;
        }

        logger.info("Flag --{} detectada. Iniciando restore e sanitização", RESTORE_FLAG);

        int exitCode = 0;
        try {
            restoreService.restoreFromLatestBackupAndSanitize();
            logger.info("Restore e sanitização concluídos com sucesso");
        } catch (Exception e) {
            logger.error("Falha durante restore e sanitização", e);
            exitCode = 1;
        }

        logger.info("Encerrando aplicação após restore (exit code: {})", exitCode);
        System.exit(SpringApplicationExit.exit(applicationContext, exitCode));
    }

    private static class SpringApplicationExit {
        static int exit(ConfigurableApplicationContext ctx, int code) {
            return org.springframework.boot.SpringApplication.exit(ctx, () -> code);
        }
    }
}
