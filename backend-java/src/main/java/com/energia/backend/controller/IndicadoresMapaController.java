package com.energia.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.energia.backend.dto.MapaCalorResponse;
import com.energia.backend.service.IndicadoresMapaService;

@RestController
@RequestMapping("/indicadores")
public class IndicadoresMapaController {

    private final IndicadoresMapaService indicadoresMapaService;

    public IndicadoresMapaController(IndicadoresMapaService indicadoresMapaService) {
        this.indicadoresMapaService = indicadoresMapaService;
    }

    @GetMapping("/mapa-calor")
    public ResponseEntity<MapaCalorResponse> obterMapaCalor(
            @RequestParam(required = false) Long ano
    ) {
        return ResponseEntity.ok(indicadoresMapaService.obterMapaCalor(ano));
    }
}