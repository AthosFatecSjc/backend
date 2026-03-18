package com.energia.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.model.Logger;
import com.energia.backend.service.LogService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Controller responsável por gerenciar operações relacionadas aos logs do sistema.
 * 
 * Fornece endpoints para:
 * - Criação de logs individuais e em lote
 * - Consulta de logs (todos, por ID, auditáveis e não auditáveis)
 * - Atualização de logs
 * - Remoção de logs
 */
@RestController
@RequestMapping("/logs")
public class LogController {

    @Autowired
    private LogService service;

    /**
     * Cria um novo log no sistema.
     *
     * @param log objeto Logger contendo os dados do log
     * @return o log salvo com ID gerado
     */
    @PostMapping
    public Logger saveLog(@RequestBody Logger log){
        return service.saveLog(log);
    }

    /**
     * Cria múltiplos logs em uma única requisição.
     *
     * @param logs lista de logs a serem salvos
     * @return lista de logs persistidos
     */
    @PostMapping("/batch")
    public List<Logger> saveLogs(@RequestBody List<Logger> logs){
        return service.saveLogs(logs);
    }

    /**
     * Retorna todos os logs cadastrados.
     *
     * @return lista de logs
     */
    @GetMapping
    public Page<Logger> getLogs(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return service.getAllLogs(pageable);
    }


    /**
     * Busca um log pelo seu ID.
     *
     * @param id identificador do log
     * @return log encontrado
     * @throws RuntimeException caso o log não exista
     */
    @GetMapping("/{id}")
    public Logger getLogById(@PathVariable Long id){
        return service.getLogById(id)
                            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log não encontrado"));
    }

    /**
     * Remove um log pelo ID.
     *
     * @param id identificador do log a ser removido
     */
    @DeleteMapping("/{id}")
    public void deleteLog(@PathVariable Long id){
        service.deleteLog(id);
    }

    /**
     * Retorna apenas logs marcados como auditáveis.
     *
     * @return lista de logs auditáveis
     */
    @GetMapping("/auditaveis")
    public Page<Logger> getAuditaveis(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        return service.getAuditaveis(pageable);
    }

    /**
     * Retorna logs que NÃO são auditáveis.
     *
     * @return lista de logs não auditáveis
     */
    @GetMapping("/nao-auditaveis")
    public Page<Logger> getNaoAuditaveis(@RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "10") int size){
        Pageable pageable = PageRequest.of(page, size);
        return service.getNaoAuditaveis(pageable);
    }
}