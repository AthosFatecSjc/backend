package com.energia.backend.service;

import com.energia.backend.model.log.LogCategory;
import com.energia.backend.model.log.LogEvent;
import com.energia.backend.model.log.ResultType;
import com.energia.backend.model.log.SourceType;
import com.energia.backend.model.log.SystemLog;
import com.energia.backend.repository.LogRepository;
import org.springframework.stereotype.Service;

@Service
public class LogService {

    private final LogRepository logRepository;

    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void log(
            String actorRef,
            String targetRef,
            SourceType sourceType,
            LogEvent event,
            ResultType result,
            LogCategory category,
            String description,
            String metadata,
            String createdByModule
    ) {
        SystemLog log = new SystemLog();
        log.setActorRef(actorRef);
        log.setTargetRef(targetRef);
        log.setSourceType(sourceType);
        log.setEvent(event);
        log.setResult(result);
        log.setLogCategory(category);
        log.setDescription(description);
        log.setMetadata(metadata);
        log.setCreatedByModule(createdByModule);
        logRepository.save(log);
    }
}
