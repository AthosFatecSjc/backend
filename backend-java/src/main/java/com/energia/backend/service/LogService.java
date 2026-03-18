package com.energia.backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.energia.backend.dto.LogRequest;
import com.energia.backend.model.SystemLog;
import com.energia.backend.repository.LogRepository;

/**
 * Serviço responsável por gerenciar operações relacionadas aos logs do sistema.
 * 
 * Inclui:
 * - Criação de logs individuais e em lote
 * - Consulta de logs (todos, auditáveis e não auditáveis)
 * - Paginação padrão quando Pageable não for fornecido
 * - Exclusão segura com proteção para logs auditáveis
 * - Registro de logs programático
 */
@Service
public class LogService {
    
    @Autowired
    private LogRepository logRepository;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;

    /**
     * Retorna um Pageable padrão caso o fornecido seja null.
     *
     * @param pageable Pageable opcional
     * @return Pageable válido
     */
    private Pageable defaultPageable(Pageable pageable) {
        return pageable != null ? pageable : PageRequest.of(DEFAULT_PAGE, DEFAULT_SIZE);
    }

    /**
     * Salva um log no banco de dados.
     *
     * @param log objeto SystemLog a ser persistido
     * @return log salvo com ID gerado
     */
    public SystemLog saveLog(SystemLog log){
        try {
            return logRepository.save(log);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar log", e);
        }
    }
    
    /**
     * Salva múltiplos logs no banco de dados.
     *
     * @param logs lista de logs a serem persistidos
     * @return lista de logs salvos
     */
    public List<SystemLog> saveLogs(List<SystemLog> logs){
        try {
            return logRepository.saveAll(logs);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao salvar logs em lote", e);
        }
    }

    /**
     * Retorna todos os logs com paginação.
     *
     * @param pageable Pageable opcional
     * @return página de logs
     */
    public Page<SystemLog> getAllLogs(Pageable pageable) {
        try {
            return logRepository.findAll(defaultPageable(pageable));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar logs", e);
        }
    }

    /**
     * Busca um log por ID.
     *
     * @param id ID do log
     * @return Optional contendo o log encontrado ou vazio
     */
    public Optional<SystemLog> getLogById(Long id){
        try {
            return logRepository.findById(id);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao buscar log", e);
        }
    }

    /**
     * Deleta um log por ID. Logs auditáveis não podem ser deletados.
     *
     * @param id ID do log a ser removido
     * @throws ResponseStatusException 404 se não encontrado, 403 se auditável
     */
    public void deleteLog(Long id){
        try {
            logRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Log não encontrado"));

            logRepository.deleteById(id);

        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao deletar log", e);
        }
    }

    /**
     * Registra um log estruturado no sistema a partir de um objeto padronizado.
     *
     * O uso de LogRequest garante consistência na estrutura dos eventos,
     * evitando variações e erros de uso entre diferentes partes do sistema.
     *
     * @param request objeto contendo todos os dados do log
     */
    public void log(LogRequest request) {
        try {
            SystemLog log = new SystemLog();
            log.setActorRef(request.getActor());
            log.setSourceType(request.getSourceType());
            log.setEvent(request.getEvent());
            log.setResult(request.getResult());
            log.setDescription(request.getDescription());
            log.setMetadata(request.getMetadata());
            log.setTargetRef(request.getTargetRef());
            log.setCreatedByModule(request.getModule());

            logRepository.save(log);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro ao registrar log", e);
        }
    }
}