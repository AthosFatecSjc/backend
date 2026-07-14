package com.energia.backend.etl;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.Set;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;

import com.energia.backend.model.aneel.ContractType;
import com.energia.backend.model.aneel.Regiao;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {
    private static final Set<String> MISSING_TOKENS = Set.of(
        "N/A", "NA", "N.D", "ND", "NULO", "NULL", "SEM INFORMACAO", "SEM INFORMAÇÃO", "-", "--"
    );

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
        String value = cleanNullable(raw);
        if (value == null) return null;

        String digits = value.replaceAll("\\D", "");
        if (digits.isBlank()) {
            return null;
        }

        try {
            return String.format("%014d", Long.parseLong(digits));
        } catch (NumberFormatException e) {
            log.warn("Valor inválido para CNPJ: {}", raw);
            return null;
        }
    }

    public static Long toLong(String raw) {
        String value = cleanNullable(raw);
        if (value == null) return null;
    
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            log.warn("Valor inválido para Long: {}", raw);
            return null;
        }
    }

    public static String cleanNullable(String raw) {
        if (raw == null) {
            return null;
        }
        String value = raw.trim();
        if (value.isEmpty()) {
            return null;
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
            .toUpperCase();
        if (MISSING_TOKENS.contains(normalized)) {
            return null;
        }
        return value;
    }

    public static Double toDoubleBrNullable(String raw) {
        String value = cleanNullable(raw);
        if (value == null) {
            return null;
        }
        String normalized = value.replace("%", "").replace(".", "").replace(",", ".");
        try {
            return Double.parseDouble(normalized);
        } catch (NumberFormatException e) {
            log.warn("Valor inválido para Double: {}", raw);
            return null;
        }
    }

    public static LocalDate toDateNullable(String raw) {
        String value = cleanNullable(raw);
        if (value == null) {
            return null;
        }
        String[] formats = {"yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy"};
        for (String fmt : formats) {
            try {
                if ("yyyy-MM-dd".equals(fmt)) {
                    String datePart = value.length() >= 10 ? value.substring(0, 10) : value;
                    return LocalDate.parse(datePart);
                }
                if ("dd/MM/yyyy".equals(fmt)) {
                    String[] p = value.split("/");
                    if (p.length == 3) {
                        return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                    }
                }
                if ("dd-MM-yyyy".equals(fmt)) {
                    String[] p = value.split("-");
                    if (p.length == 3) {
                        return LocalDate.of(Integer.parseInt(p[2]), Integer.parseInt(p[1]), Integer.parseInt(p[0]));
                    }
                }
            } catch (DateTimeException | NumberFormatException ignored) {
            }
        }
        log.warn("Valor inválido para LocalDate: {}", raw);
        return null;
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
