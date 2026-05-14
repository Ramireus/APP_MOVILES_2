package com.example.controlgastosapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room — persiste gastos en SQLite local.
 * HU-05: El usuario puede registrar gastos sin conexión a internet.
 *
 * [mongoId]       — ID asignado por MongoDB al sincronizar con la API
 * [sincronizado]  — false = pendiente de subir a la API y Firestore
 */
@Entity(tableName = "gastos")
data class GastoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val valor: Double,
    val categoria: String,
    val fecha: Long = System.currentTimeMillis(),
    val mongoId: String = "",
    val sincronizado: Boolean = false
)
