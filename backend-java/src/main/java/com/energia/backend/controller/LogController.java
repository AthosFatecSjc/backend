package com.energia.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.energia.backend.model.Logger;
import com.energia.backend.service.LogService;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService service;

    @PostMapping
    public Logger saveLog(@RequestBody Logger log){
        return service.saveLog(log);
    }

    @PostMapping("/batch")
    public List<Logger> saveLogs(@RequestBody List<Logger> logs){
        return service.saveLogs(logs);
    }

    @GetMapping
    public List<Logger> getAllLogs(){
        return service.getAllLogs();
    }

    @GetMapping("/{id}")
    public Logger getLogById(@PathVariable Long id){
        return service.getLogById(id)
                .orElseThrow(() -> new RuntimeException("Log não encontrado"));
    }

    @PutMapping("/{id}")
    public Logger updateLog(@PathVariable Long id, @RequestBody Logger log){
        return service.updateLog(id, log);
    }

    @DeleteMapping("/{id}")
    public void deleteLog(@PathVariable Long id){
        service.deleteLog(id);
    }

    @GetMapping("/auditaveis")
    public List<Logger> getAuditaveis(){
        return service.getAuditaveis();
    }

    @GetMapping("/nao-auditaveis")
    public List<Logger> getNaoAuditaveis(){
        return service.getNaoAuditaveis();
    }
}