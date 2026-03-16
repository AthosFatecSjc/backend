package com.energia.backend.controller;

import com.energia.backend.model.Concessionaria;
import com.energia.backend.repository.ConcessionariaRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/concessionarias")
public class ConcessionariaController {
    private final ConcessionariaRepository repository;
    public ConcessionariaController(ConcessionariaRepository repository) {
        this.repository = repository;
    }
    @GetMapping
    public List<Concessionaria> getAll() {
        return repository.findAll();
    }
}
