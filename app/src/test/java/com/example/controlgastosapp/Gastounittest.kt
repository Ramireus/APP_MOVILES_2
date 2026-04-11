package com.example.controlgastosapp

import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Test

class GastoUnitTest {

    // ─────────────────────────────────────
    // HU-01: Validación de gastos
    // ─────────────────────────────────────

    @Test
    fun gastoValidoEsAceptado() {
        assertTrue(GastoValidator.esValido("Almuerzo", 15_000.0))
    }

    @Test
    fun gastoConNombreVacioEsRechazado() {
        assertFalse(GastoValidator.esValido("", 15_000.0))
    }

    @Test
    fun gastoConValorNegativoEsRechazado() {
        assertFalse(GastoValidator.esValido("Café", -5_000.0))
    }

    @Test
    fun gastoConValorCeroEsRechazado() {
        assertFalse(GastoValidator.esValido("Test", 0.0))
    }

    @Test
    fun sumaTotalDeGastosEsCorrecta() {
        val lista = listOf(
            Gasto("Café", 5_000.0, "Comida"),
            Gasto("Bus", 2_600.0, "Transporte"),
            Gasto("Almuerzo", 15_000.0, "Comida")
        )
        assertEquals(22_600.0, lista.sumOf { it.valor }, 0.01)
    }

    // ─────────────────────────────────────
    // HU-02: Categorización
    // ─────────────────────────────────────

    @Test
    fun gastosSeAgrupanCorrectamentePorCategoria() {
        val lista = listOf(
            Gasto("Café", 5_000.0, "Comida"),
            Gasto("Bus", 2_600.0, "Transporte"),
            Gasto("Almuerzo", 15_000.0, "Comida")
        )
        val porCategoria = lista.groupBy { it.categoria }
        assertEquals(2, porCategoria["Comida"]?.size)
        assertEquals(1, porCategoria["Transporte"]?.size)
    }

    // ─────────────────────────────────────
    // HU-04: Lógica de umbrales
    // ─────────────────────────────────────

    @Test
    fun porcentajeSeCalculaCorrectamente() {
        assertEquals(
            0.75,
            GastoValidator.calcularPorcentaje(150_000.0, 200_000.0),
            0.001
        )
    }

    @Test
    fun umbral80ActivaNotificacion() {
        assertTrue(GastoValidator.superaUmbral(400_000.0, 500_000.0))
    }

    @Test
    fun umbral79NoActivaNotificacion() {
        assertFalse(GastoValidator.superaUmbral(395_000.0, 500_000.0))
    }

    @Test
    fun porcentajeNoSuperaUnoConExceso() {
        assertEquals(
            1.0,
            GastoValidator.calcularPorcentaje(900_000.0, 500_000.0),
            0.001
        )
    }

    @Test
    fun mensajeAlertaContieneDatos() {
        val msg = GastoValidator.mensajeAlerta("Comida", 0.85)
        assertTrue(msg.contains("Comida"))
        assertTrue(msg.contains("85"))
    }
}