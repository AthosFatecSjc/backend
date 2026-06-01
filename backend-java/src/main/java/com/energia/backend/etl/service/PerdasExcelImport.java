package com.energia.backend.etl.service;

import java.io.FileInputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.energia.backend.etl.Utils;
import com.energia.backend.model.aneel.Distribuidora;
import com.energia.backend.model.aneel.Perdas;
import com.energia.backend.repository.aneel.DistribuidoraRepository;
import com.energia.backend.repository.aneel.PerdasRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PerdasExcelImport {

    private final PerdasRepository perdasRepository;
    private final DistribuidoraRepository distribuidoraRepository;

    @Value("${ETL_FILE_PERDAS_PATH:}")
    private String caminhoArquivo;

    @Value("${etl_cnpj:}")
    private String etlCnpj;

    // Estrutura do Excel: linha 0=filtros, 1=vazia, 2=cabeçalhos, 3+=dados
    private static final int DATA_START_ROW = 3;
    private static final int COL_SIGLA      = 13;
    private static final int COL_DATA       = 3;
    private static final int COL_PERDAS     = 7;
    private static final int COL_CUSTO      = 10;

    public void importar() {
        if (caminhoArquivo == null || caminhoArquivo.isBlank()) {
            throw new IllegalStateException("ETL_FILE_PERDAS_PATH não está definida");
        }
        if (etlCnpj == null || etlCnpj.isBlank()) {
            throw new IllegalStateException("ETL_CNPJ não está definida");
        }

        String cnpjFormatado = Utils.formatCnpj(etlCnpj);
        if (cnpjFormatado == null) {
            throw new IllegalStateException("ETL_CNPJ inválido: " + etlCnpj);
        }

        Distribuidora distribuidora = distribuidoraRepository.findByNumCnpj(cnpjFormatado)
            .orElseThrow(() -> new IllegalStateException(
                "Distribuidora com CNPJ " + etlCnpj + " não encontrada. " +
                "Execute o ETL principal antes de importar perdas."));

        String sigAgenteNorm = normalizar(distribuidora.getSigAgente());
        log.info("Importando perdas para: {} (sig_agente norm: {})", distribuidora.getSigAgente(), sigAgenteNorm);

        try (FileInputStream fis = new FileInputStream(caminhoArquivo);
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            List<Perdas> lista = new ArrayList<>();
            int ignoradas = 0;
            int duplicadas = 0;
            int novas = 0;

            for (int i = DATA_START_ROW; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String siglaExcel = normalizar(Utils.getRaw(row.getCell(COL_SIGLA)));
                if (!bate(siglaExcel, sigAgenteNorm)) {
                    ignoradas++;
                    continue;
                }

                LocalDate dataProcesso = lerData(row);
                if (dataProcesso == null) {
                    log.warn("Linha {} ignorada: data inválida", i + 1);
                    continue;
                }

                long ano = dataProcesso.getYear();

                if (perdasRepository.existsByDistribuidoraIdAndAno(distribuidora.getId(), ano)) {
                    log.debug("Perdas já existem para {} ano {}", distribuidora.getSigAgente(), ano);
                    duplicadas++;
                    continue;
                }

                Perdas perdas = new Perdas();
                perdas.setDistribuidora(distribuidora);
                perdas.setDataProcesso(dataProcesso);
                perdas.setAno(ano);
                perdas.setPerdasNaoTec(lerDouble(row, COL_PERDAS));
                perdas.setCustoPerdasNaoTec(lerDouble(row, COL_CUSTO));

                lista.add(perdas);
                novas++;
            }

            perdasRepository.saveAll(lista);

            log.info("Importação de perdas finalizada. Novas: {}, duplicadas: {}, de outras distribuidoras: {}",
                novas, duplicadas, ignoradas);

        } catch (Exception e) {
            log.error("Erro ao importar Excel de perdas", e);
            throw new IllegalStateException("Falha ao importar perdas do Excel: " + e.getMessage(), e);
        }
    }

    // --- helpers ---

    private static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) return "";
        String nfkd = Normalizer.normalize(valor, Normalizer.Form.NFKD);
        String ascii = nfkd.replaceAll("[^\\p{ASCII}]", "");
        String limpo = ascii.toUpperCase().replaceAll("[^A-Z0-9]", " ");
        return limpo.trim().replaceAll("\\s+", " ");
    }

    private static boolean bate(String excelNorm, String sigAgenteNorm) {
        if (excelNorm.isEmpty() || sigAgenteNorm.isEmpty()) return false;

        // 1. match exato após normalização
        if (excelNorm.equals(sigAgenteNorm)) return true;

        // 2. excel contém sig_agente ou sig_agente contém excel
        if (excelNorm.contains(sigAgenteNorm) || sigAgenteNorm.contains(excelNorm)) return true;

        // 3. todos os tokens significativos (>= 3 chars) do sig_agente aparecem no excel
        String[] tokens = sigAgenteNorm.split(" ");
        for (String token : tokens) {
            if (token.length() >= 3 && !excelNorm.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private static LocalDate lerData(Row row) {
        var cell = row.getCell(COL_DATA);
        if (cell == null) return null;
        try {
            if (DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue().toLocalDate();
            }
            return Utils.toDateNullable(Utils.getRaw(cell));
        } catch (Exception e) {
            return null;
        }
    }

    private static Double lerDouble(Row row, int col) {
        var cell = row.getCell(col);
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> cell.getNumericCellValue();
                case STRING  -> Utils.toDoubleBrNullable(cell.getStringCellValue());
                default      -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }
}
