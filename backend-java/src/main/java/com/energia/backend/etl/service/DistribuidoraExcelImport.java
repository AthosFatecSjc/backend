package com.energia.backend.etl.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import com.energia.backend.etl.Utils;
import com.energia.backend.model.aneel.Distribuidora;
import com.energia.backend.repository.aneel.DistribuidoraRepository;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DistribuidoraExcelImport {

    private final DistribuidoraRepository repository;

    public void importar(String caminhoArquivo) {

        try (FileInputStream fis = new FileInputStream(caminhoArquivo);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            List<Distribuidora> lista = new ArrayList<>();

            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; 

                Distribuidora dist = mapRowToEntity(row);

                if (dist != null) {
                    lista.add(dist);
                }
            }

            repository.saveAll(lista);

            log.info("Importação finalizada. Total: {}", lista.size());

        } catch (Exception e) {
            log.error("Erro ao importar Excel", e);
        }
    }

    private Distribuidora mapRowToEntity(Row row) {

        Distribuidora dist = new Distribuidora();

        String codigoIdDistRaw = Utils.getRaw(row.getCell(13));
        String sigAgenteRaw = Utils.getRaw(row.getCell(1));             
        String cnpjRaw = Utils.getRaw(row.getCell(10));
        String razaoSocialRaw = Utils.getRaw(row.getCell(11));
        String regiaoRaw = Utils.getRaw(row.getCell(8));
        String ufRaw = Utils.getRaw(row.getCell(7));
        String contractTypeRaw = Utils.getRaw(row.getCell(12));
      

        dist.setCodigoIdDist(Utils.toLong(codigoIdDistRaw));
        dist.setSigAgente((sigAgenteRaw));
        dist.setNumCnpj(Utils.formatCnpj(cnpjRaw));
        dist.setRazaoSocial((razaoSocialRaw));
        dist.setRegiao(Utils.parseRegiao(regiaoRaw));
        dist.setUf(Utils.formatUf(ufRaw));
        dist.setContractType(Utils.parseContractType(contractTypeRaw));

        return dist;
    }
 

}