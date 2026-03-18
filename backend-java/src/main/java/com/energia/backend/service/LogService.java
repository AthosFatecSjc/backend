package com.energia.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.energia.backend.model.LogLevel;
import com.energia.backend.model.Logger;
import com.energia.backend.repository.LogRepository;

@Service
public class LogService {
    
    @Autowired
    private LogRepository logRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    private Pageable defaultPageable(Pageable pageable) {
        return pageable != null ? pageable : PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    public Logger saveLog(Logger log){
        return logRepository.save(log);
    }
    
    public List<Logger> saveLogs(List<Logger> logs){
        return logRepository.saveAll(logs);
    }

    public Page<Logger> getAllLogs(Pageable pageable) {
        return logRepository.findAll(defaultPageable(pageable));
    }

    public Optional<Logger> getLogById(Long id){
        return logRepository.findById(id);
    }

    public void deleteLog(Long id){
        logRepository.deleteById(id);
    }

    public Page<Logger> getAuditaveis(Pageable pageable){
        return logRepository.findByIsAuditavelTrue(defaultPageable(pageable));
    }

    public Page<Logger> getNaoAuditaveis(Pageable pageable){
        return logRepository.findByIsAuditavelFalse(defaultPageable(pageable));
    }

    public void log(String actor, Boolean auditavel, LogLevel level, String conteudo){
        Logger log = new Logger();
        log.setActorRef(actor);
        log.setIsAuditavel(auditavel);
        log.setLevel(level);
        log.setConteudo(conteudo);

        logRepository.save(log);
    }
}