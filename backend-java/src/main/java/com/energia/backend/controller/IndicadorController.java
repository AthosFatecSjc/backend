package com.energia.backend.controller;

import com.energia.backend.model.Indicador;
import com.energia.backend.repository.IndicadorRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/indicadores")
public class IndicadorController {
    private final IndicadorRepository repository;
    public IndicadorController(IndicadorRepository repository) {
        this.repository = repository;
    }
    @GetMapping
    public List<Indicador> getAll() {
        return repository.findAll();
    }
    @GetMapping("/{id}")
    public Optional<Indicador> getById(@PathVariable Long id) {
        return repository.findById(id);
    }
    @PostMapping
    public Indicador create(@RequestBody Indicador indicador) {
        return repository.save(indicador);
    }
    @PutMapping("/{id}")
    public Indicador update(@PathVariable Long id, @RequestBody Indicador indicador) {
        indicador.setId(id);
        return repository.save(indicador);
    }
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
