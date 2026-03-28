package com.example.controlgastosapp

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.controlgastosapp.ui.theme.ControlGastosAppTheme

class MainActivity : ComponentActivity() {

    val listaGastos = mutableListOf<Gasto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 🔥 PRUEBAS COMPLETAS (Sprint 1)
        agregarGasto("Almuerzo", 15000.0, "Comida")
        agregarGasto("Bus", 3000.0, "Transporte")
        agregarGasto("Cena", 20000.0, "Comida")
        agregarGasto("Taxi", 10000.0, "Transporte")
        agregarGasto("Error", -5000.0, "Test") // validación

        val total = calcularTotal()

        Log.d("TOTAL", "Total gastos: $total")

        setContent {
            ControlGastosAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Total de gastos: $total COP",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    fun agregarGasto(nombre: String, valor: Double, categoria: String) {
        if (valor <= 0) {
            Log.d("ERROR", "Valor inválido")
            return
        }
        listaGastos.add(Gasto(nombre, valor, categoria))
    }

    fun calcularTotal(): Double {
        var total = 0.0
        for (g in listaGastos) {
            total += g.valor
        }
        return total
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = name,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ControlGastosAppTheme {
        Greeting("Vista previa")
    }
}