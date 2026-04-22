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
import java.util.Objects;

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
            int linhasValidas = 0;

            for (Row row : sheet) {

                if (row.getRowNum() == 0) continue; 

                Long codigoIdDist = Utils.toLong(Utils.getRaw(row.getCell(13))); 
                if (codigoIdDist == null) {
                    log.warn("Linha {} ignorada: cod_id_dist inválido ou vazio", row.getRowNum());
                    continue;
                }
                if(repository.existsByCodigoIdDist(codigoIdDist)){
                    continue;
                }
                

                Distribuidora dist = mapRowToEntity(row);

                
                if (dist != null) {
                    lista.add(dist);
                    linhasValidas++;
                }
            }

            if (linhasValidas == 0) {
                throw new IllegalStateException("Erro na extração ANEEL: planilha de distribuidoras sem registros válidos");
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

        Long codigoIdDist = Utils.toLong(codigoIdDistRaw);
        String sigAgente = Utils.cleanNullable(sigAgenteRaw);
        String cnpj = Utils.formatCnpj(cnpjRaw);

        if (codigoIdDist == null || sigAgente == null || cnpj == null) {
            log.warn("Linha {} ignorada por campos essenciais inválidos", row.getRowNum());
            return null;
        }

        dist.setCodigoIdDist(codigoIdDist);
        dist.setSigAgente(sigAgente);
        dist.setNumCnpj(cnpj);
        dist.setRazaoSocial(Utils.cleanNullable(razaoSocialRaw));
        dist.setRegiao(Utils.parseRegiao(regiaoRaw));
        dist.setUf(Utils.formatUf(ufRaw));
        dist.setContractType(Utils.parseContractType(contractTypeRaw));

        if (Objects.isNull(dist.getRegiao()) || Objects.isNull(dist.getUf()) || Objects.isNull(dist.getContractType())) {
            log.warn(
                "Linha {} com dimensões faltantes para distribuidora {} (regiao/uf/contract_type nulos).",
                row.getRowNum(),
                dist.getNumCnpj()
            );
        }

        return dist;
    }
 

}