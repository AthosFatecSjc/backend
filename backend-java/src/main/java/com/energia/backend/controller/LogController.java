package com.energia.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.energia.backend.model.Logger;
import com.energia.backend.service.LogService;

@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService service;

    @PostMapping("/saveLog")
    public void saveLog(@RequestBody Logger log){
        service.saveLog(log);
    }
}
