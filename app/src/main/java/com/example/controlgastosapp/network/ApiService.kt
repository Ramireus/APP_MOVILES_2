package com.example.controlgastosapp.network

import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

// ─── Modelos de datos de la API ───────────────────────────────

data class GastoApi(
    @SerializedName("_id") val id: String = "",
    val nombre: String,
    val valor: Double,
    val categoria: String,
    val fecha: String = ""
)

data class ListaGastosResponse(
    val gastos: List<GastoApi>,
    val total: Int
)

data class PresupuestoRequest(
    val categoria: String,
    val limite: Double,
    val umbralAlerta: Int = 80
)

data class AlertaCategoria(
    val categoria: String,
    val limite: Double,
    val gastado: Double,
    val porcentaje: Int,
    val superaUmbral: Boolean,
    val umbral: Int,
    val disponible: Double
)

data class VerificarResponse(
    val alertas: List<AlertaCategoria>,
    val enAlerta: List<AlertaCategoria>,
    val hayAlertas: Boolean
)

data class ReporteMensual(
    val mes: String,
    val total: Double,
    val cantidad: Int,
    val promedio: Int
)

data class CategoriaReporte(
    val categoria: String,
    val total: Double,
    val cantidad: Int,
    val promedio: Int
)

data class ReporteCategorias(
    val categorias: List<CategoriaReporte>,
    val totalGeneral: Double
)

// ─── Interfaz Retrofit ───────────────────────────────────────

interface ApiService {

    @GET("gastos")
    suspend fun listarGastos(): Response<ListaGastosResponse>

    @POST("gastos")
    suspend fun crearGasto(@Body gasto: GastoApi): Response<GastoApi>

    @DELETE("gastos/{id}")
    suspend fun eliminarGasto(@Path("id") id: String): Response<Map<String, String>>

    @POST("presupuestos")
    suspend fun guardarPresupuesto(@Body p: PresupuestoRequest): Response<PresupuestoRequest>

    @GET("presupuestos/verificar")
    suspend fun verificarUmbrales(): Response<VerificarResponse>

    @GET("reportes/mensual")
    suspend fun reporteMensual(): Response<ReporteMensual>

    @GET("reportes/categorias")
    suspend fun reporteCategorias(): Response<ReporteCategorias>

    companion object {
        // Emulador: 10.0.2.2 → localhost del PC
        // Dispositivo físico: URL de NGROK
        private const val BASE_URL = "https://unfocused-atlantic-retract.ngrok-free.dev"

        fun crear(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(OkHttpClient.Builder().addInterceptor(logging).build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
