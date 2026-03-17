package com.energia.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.energia.backend.model.Logger;

import com.energia.backend.repository.LogRepository;

@Service
public class LogService {
    
    @Autowired
    private LogRepository logRepository;

    public void saveLog(Logger log){
        try {
            logRepository.save(log);
            System.out.println("log salvo");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void saveLogs(List<Logger> logs){

        try {
            logRepository.saveAll(logs);    
        } catch (Exception e) {
            System.out.println("Erro ao salvar logs");
        }
        
    }
}
