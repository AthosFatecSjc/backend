package com.energia.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.energia.backend.model.LogLevel;
import com.energia.backend.model.Logger;
import com.energia.backend.repository.LogRepository;

@Service
public class LogService {
    
    @Autowired
    private LogRepository logRepository;

    public Logger saveLog(Logger log){
        return logRepository.save(log);
    }
    
    public List<Logger> saveLogs(List<Logger> logs){
        return logRepository.saveAll(logs);
    }

    public List<Logger> getAllLogs(){
        return logRepository.findAll();
    }

    public Optional<Logger> getLogById(Long id){
        return logRepository.findById(id);
    }

    public Logger updateLog(Long id, Logger newLog){
        return logRepository.findById(id).map(log -> {
            log.setActorRef(newLog.getActorRef());
            log.setIsAuditavel(newLog.getIsAuditavel());
            log.setLevel(newLog.getLevel());
            log.setConteudo(newLog.getConteudo());
            log.setDateTime(newLog.getDateTime());
            return logRepository.save(log);
        }).orElseThrow(() -> new RuntimeException("Log não encontrado"));
    }

    public void deleteLog(Long id){
        logRepository.deleteById(id);
    }

    public List<Logger> getAuditaveis(){
    return logRepository.findByIsAuditavelTrue();
    }

    public List<Logger> getNaoAuditaveis(){
        return logRepository.findByIsAuditavelFalse();
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