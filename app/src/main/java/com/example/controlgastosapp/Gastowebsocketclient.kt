package com.example.controlgastosapp

import android.util.Log
import kotlinx.coroutines.*
import okhttp3.*
import org.json.JSONObject

class GastoWebSocketClient(
    private val url: String,
    var onAlertaRecibida: ((String) -> Unit)? = null
) {
    private val TAG = "GastoWebSocket"
    private val cliente = OkHttpClient()
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    fun conectar() {
        val request = Request.Builder().url(url).build()

        webSocket = cliente.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "✅ WebSocket conectado")
                webSocket.send(JSONObject().apply {
                    put("tipo", "CONECTADO")
                    put("cliente", "ControlGastosApp")
                }.toString())
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "📩 Mensaje: $text")
                try {
                    val json = JSONObject(text)
                    if (json.optString("tipo") == "ALERTA") {
                        val mensaje = json.optString("mensaje", "Alerta de gasto")
                        scope.launch(Dispatchers.Main) {
                            onAlertaRecibida?.invoke(mensaje)
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al parsear: ${e.message}")
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "❌ Error: ${t.message}")
                scope.launch {
                    delay(5_000)
                    conectar()
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "🔌 Cerrado: $reason")
            }
        })
    }

    fun enviarAlerta(categoria: String, porcentaje: Double) {
        val msg = JSONObject().apply {
            put("tipo", "GASTO_UMBRAL")
            put("categoria", categoria)
            put("porcentaje", (porcentaje * 100).toInt())
            put("timestamp", System.currentTimeMillis())
        }.toString()
        webSocket?.send(msg)
    }

    fun desconectar() {
        webSocket?.close(1000, "App cerrada")
        scope.cancel()
    }
}