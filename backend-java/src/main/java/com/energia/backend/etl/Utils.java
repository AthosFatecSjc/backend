package com.energia.backend.etl;

import java.text.Normalizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import com.energia.backend.model.aneel.ContractType;
import com.energia.backend.model.aneel.Regiao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {
    public static String getRaw(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {

            case STRING -> cell.getStringCellValue().trim();

            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue()
                              .toLocalDate()
                              .toString();
                } else {
                    yield String.valueOf((long) cell.getNumericCellValue());
                }
            }

            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());

            default -> null;
        };
    }

    
    public static String formatCnpj(String raw) {
        if (raw == null || raw.isBlank()) return null;

        raw = raw.replaceAll("\\D", "");

        return String.format("%014d", Long.parseLong(raw));
    }

    public static Long toLong(String raw) {
        if (raw == null || raw.isBlank()) return null;
    
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            log.warn("Valor inválido para Long: {}", raw);
            return null;
        }
    }

    public static Regiao parseRegiao(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String normalizado = raw
                .trim()
                .toUpperCase()
                .replace(" ", "_")
                .replace("-", "_");

        try {
            return Regiao.valueOf(normalizado);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static String formatUf(String raw) {
        if (raw == null || raw.isBlank()) return null;
    
        String uf = raw.trim().toUpperCase();
    
        if (uf.length() != 2) return null;
    
        return uf;
    }

    public static ContractType parseContractType(String raw) {
        if (raw == null || raw.isBlank()) return null;

        String normalizado = Normalizer.normalize(raw, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") 
                .toUpperCase()
                .trim();

        try {
            return ContractType.valueOf(normalizado);
        } catch (IllegalArgumentException e) {
            return null;
        }
        }

}
