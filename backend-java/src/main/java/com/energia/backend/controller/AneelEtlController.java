package com.energia.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.energia.backend.etl.service.geographic.EtlService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/aneel/etl")
@RequiredArgsConstructor
public class AneelEtlController {

    private final EtlService importService;

    @PostMapping("/processar")
    public ResponseEntity<String> processar(@RequestParam String cnpj) {

        try {
            importService.executarPipeline(cnpj);

            return ResponseEntity.ok(
                "Processamento realizado com sucesso para o CNPJ: " + cnpj
            );

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Erro: " + e.getMessage());
        }
    }
}