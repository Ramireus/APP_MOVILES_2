package com.example.controlgastosapp

import java.util.Date

/**
 * Modelo de datos para un gasto.
 * Sprint 3: Se agrega campo `fecha` para historial y filtros.
 */
data class Gasto(
    val nombre: String,
    val valor: Double,
    val categoria: String,
    val fecha: Date = Date()
)