package com.example.controlgastosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.controlgastosapp.ui.theme.ControlGastosAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ControlGastosAppTheme {
                PantallaGastos()
            }
        }
    }
}

@Composable
fun PantallaGastos() {

    val listaGastos = remember { mutableStateListOf<Gasto>() }

    var nombre by remember { mutableStateOf("") }
    var valor by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf("") }

    val total = listaGastos.sumOf { it.valor }

    Column(modifier = Modifier.padding(16.dp)) {

        Text(text = "Control de Gastos", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )

        OutlinedTextField(
            value = valor,
            onValueChange = { valor = it },
            label = { Text("Valor") }
        )

        OutlinedTextField(
            value = categoria,
            onValueChange = { categoria = it },
            label = { Text("Categoría") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val valorDouble = valor.toDoubleOrNull()

            if (nombre.isNotEmpty() && valorDouble != null && valorDouble > 0) {
                listaGastos.add(Gasto(nombre, valorDouble, categoria))

                nombre = ""
                valor = ""
                categoria = ""
            }
        }) {
            Text("Agregar gasto")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Total: $total COP")

        Spacer(modifier = Modifier.height(16.dp))

        listaGastos.forEach {
            Text("- ${it.nombre}: ${it.valor} (${it.categoria})")
        }
    }
}