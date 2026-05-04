package com.energia.backend.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

class LimitesCsvParserTest {

    private final LimitesCsvParser parser = new LimitesCsvParser();

    @Test
    void deveProcessarLinhasValidasEPreservarZeroEValoresFaltantes() throws IOException {
        String csv = String.join("\n",
            "DatGeracao;A;B;IdeConj;D;SigIndicador;Ano;Valor",
            "2024-01-01;x;x;12722;x;DEC;2024;0",
            "2024-01-01;x;x;12722;x;FEC;2024;",
            "2024-01-01;x;x;99999;x;DEC;2024;5",
            "2024-01-01;x;x;12722;x;;2024;8"
        );

        List<LimitesCsvParser.LimiteFiltrado> result = parser.parsearFiltrando(csv, List.of(12722L));

        assertEquals(2, result.size());
        assertEquals(0.0, result.get(0).vlrLimite());
        assertNull(result.get(1).vlrLimite());
    }

    @Test
    void deveFalharQuandoExtracaoVierVazia() {
        String csv = "DatGeracao;A;B;IdeConj;D;SigIndicador;Ano;Valor\n";

        assertThrows(IllegalStateException.class, () -> parser.parsearFiltrando(csv, List.of(12722L)));
    }

    @Test
    void deveFalharQuandoNenhumaLinhaValidaForProcessada() {
        String csv = String.join("\n",
            "DatGeracao;A;B;IdeConj;D;SigIndicador;Ano;Valor",
            "2024-01-01;x;x;12722;x;;2024;10",
            "2024-01-01;x;x;12722;x;DEC;;10"
        );

        assertThrows(IllegalStateException.class, () -> parser.parsearFiltrando(csv, List.of(12722L)));
    }
}
