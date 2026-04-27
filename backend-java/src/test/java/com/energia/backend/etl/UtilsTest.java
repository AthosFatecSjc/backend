package com.energia.backend.etl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class UtilsTest {

    @Test
    void deveRetornarNullParaTokensDeDadoFaltante() {
        assertNull(Utils.cleanNullable(""));
        assertNull(Utils.cleanNullable("  "));
        assertNull(Utils.cleanNullable("N/A"));
        assertNull(Utils.cleanNullable("--"));
        assertNull(Utils.cleanNullable("sem informação"));
    }

    @Test
    void devePreservarZeroAoConverterNumero() {
        assertEquals(0.0, Utils.toDoubleBrNullable("0"));
        assertEquals(0.0, Utils.toDoubleBrNullable("0,00"));
        assertEquals(1234.56, Utils.toDoubleBrNullable("1.234,56"));
    }

    @Test
    void deveRetornarNullParaNumeroInvalidoOuFaltante() {
        assertNull(Utils.toDoubleBrNullable("N/A"));
        assertNull(Utils.toDoubleBrNullable("-"));
        assertNull(Utils.toDoubleBrNullable("abc"));
    }

    @Test
    void deveConverterDatasEmFormatosSuportados() {
        assertEquals(LocalDate.of(2024, 4, 22), Utils.toDateNullable("2024-04-22"));
        assertEquals(LocalDate.of(2024, 4, 22), Utils.toDateNullable("22/04/2024"));
        assertEquals(LocalDate.of(2024, 4, 22), Utils.toDateNullable("22-04-2024"));
    }

    @Test
    void deveRetornarNullParaDataInvalidaOuFaltante() {
        assertNull(Utils.toDateNullable("N/A"));
        assertNull(Utils.toDateNullable("31/02/2024"));
    }
}
