package com.example.controlgastosapp.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO Room — todas las operaciones SQLite.
 * Flow<> permite que la UI se actualice automáticamente al cambiar datos.
 */
@Dao
interface GastoDao {

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    fun observarTodos(): Flow<List<GastoEntity>>

    @Query("SELECT * FROM gastos ORDER BY fecha DESC")
    suspend fun listarTodos(): List<GastoEntity>

    @Query("SELECT * FROM gastos WHERE categoria = :categoria ORDER BY fecha DESC")
    suspend fun listarPorCategoria(categoria: String): List<GastoEntity>

    @Query("SELECT SUM(valor) FROM gastos")
    suspend fun totalGeneral(): Double?

    @Query("SELECT SUM(valor) FROM gastos WHERE categoria = :categoria")
    suspend fun totalPorCategoria(categoria: String): Double?

    @Query("SELECT * FROM gastos WHERE sincronizado = 0")
    suspend fun pendientesDeSinc(): List<GastoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(gasto: GastoEntity): Long

    @Delete
    suspend fun eliminar(gasto: GastoEntity)

    @Query("UPDATE gastos SET sincronizado = 1, mongoId = :mongoId WHERE id = :id")
    suspend fun marcarSincronizado(id: Int, mongoId: String)

    @Query("DELETE FROM gastos")
    suspend fun eliminarTodos()
}
