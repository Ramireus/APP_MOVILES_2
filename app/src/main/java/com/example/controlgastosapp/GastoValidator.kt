package com.example.controlgastosapp

/**
 * Validaciones de lógica de negocio — Sprint 3
 * NOTA: Este archivo va en app/src/main/java/...
 * Las pruebas JUnit reales van en app/src/test/java/...
 *
 * Este objeto centraliza las funciones de validación
 * que también pueden usarse desde la UI.
 */
object GastoValidator {

    /** Devuelve true si el gasto tiene datos válidos */
    fun esValido(nombre: String, valor: Double?): Boolean {
        return nombre.isNotBlank() && valor != null && valor > 0
    }

    /** Calcula el porcentaje gastado de una categoría (0.0 a 1.0) */
    fun calcularPorcentaje(gastado: Double, presupuesto: Double): Double {
        if (presupuesto <= 0) return 0.0
        return (gastado / presupuesto).coerceIn(0.0, 1.0)
    }

    /** Retorna true si se debe disparar la notificación */
    fun superaUmbral(gastado: Double, presupuesto: Double, umbral: Double = 0.8): Boolean {
        return calcularPorcentaje(gastado, presupuesto) >= umbral
    }

    /** Genera el texto del mensaje de alerta */
    fun mensajeAlerta(categoria: String, porcentaje: Double): String {
        return "⚠️ Superaste el ${(porcentaje * 100).toInt()}% del presupuesto en $categoria"
    }
}
