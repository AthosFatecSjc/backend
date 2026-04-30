package com.energia.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.energia.backend.dto.CriticidadeMapaResponse;
import com.energia.backend.service.IndicadorMapaService;

@RestController
@RequestMapping("/indicadores/mapa")
public class IndicadorMapaController {

    private final IndicadorMapaService indicadorMapaService;

    public IndicadorMapaController(IndicadorMapaService indicadorMapaService) {
        this.indicadorMapaService = indicadorMapaService;
    }

    @GetMapping("/criticidade")
    public ResponseEntity<List<CriticidadeMapaResponse>> buscarCriticidade(
            @RequestParam Long mes,
            @RequestParam Long ano,
            @RequestParam(required = false) String nomeConjunto
    ) {
        return ResponseEntity.ok(indicadorMapaService.buscarCriticidade(mes, ano, nomeConjunto));
    }
}
