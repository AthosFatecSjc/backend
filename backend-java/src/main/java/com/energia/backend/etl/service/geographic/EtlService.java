package com.energia.backend.etl.service.geographic;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.energia.backend.model.aneel.Distribuidora;
import com.energia.backend.repository.aneel.DistribuidoraRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EtlService {

    private final IdSearchService searchService;
    private final DistDownloadService downloadService;
    private final GeoProcessingService geoService;
    private final DistribuidoraRepository distribuidoraRepository;
    private final GeoJsonLoadService geoJsonLoadService;

    public void executarPipeline(String cnpj) throws IOException, InterruptedException {

        Distribuidora dist = distribuidoraRepository.findByNumCnpj(cnpj)
                .orElseThrow(() -> new IllegalStateException(
                        "Distribuidora não encontrada para o CNPJ: " + cnpj));

        String sigAgente = dist.getSigAgente();

        if (sigAgente == null || sigAgente.isBlank()) {
            throw new IllegalStateException(
                    "sig_agente não encontrado para a distribuidora: " + cnpj);
        }
        String itemId = searchService.localizarItemId(sigAgente);
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalStateException(
                    "Nenhum item encontrado no ArcGIS para: " + sigAgente);
        }

        InMemoryZip zip = downloadService.downloadGdb(itemId);
        InMemoryGdb gdb = downloadService.unzip(zip);
        
        String geoJson = geoService.converterParaGeoJson(gdb);

        geoJsonLoadService.importar(geoJson, dist); 
    }

}
