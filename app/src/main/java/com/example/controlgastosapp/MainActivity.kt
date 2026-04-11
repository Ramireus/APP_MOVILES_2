package com.example.controlgastosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.controlgastosapp.ui.theme.ControlGastosAppTheme
import java.text.NumberFormat
import java.util.*

class MainActivity : ComponentActivity() {

    private lateinit var notificationService: NotificationService
    private var webSocketClient: GastoWebSocketClient? = null  // ← nullable para evitar crash

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        notificationService = NotificationService(this)

        // ── WebSocket: solo se conecta si hay una URL real configurada ──
        val wsUrl = ""  // ← Deja vacío hasta tener NGROK. Pon aquí tu URL cuando la tengas.

        if (wsUrl.isNotBlank()) {
            webSocketClient = GastoWebSocketClient(
                url = wsUrl,
                onAlertaRecibida = { mensaje ->
                    notificationService.mostrarNotificacion("⚠️ Alerta de Gasto", mensaje)
                }
            )
        }

        setContent {
            ControlGastosAppTheme {
                AppNavigation(
                    notificationService = notificationService,
                    webSocketClient = webSocketClient  // puede ser null, la app funciona igual
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient?.desconectar()
    }
}

// ─────────────────────────────────────────────────────────────
// NAVEGACIÓN PRINCIPAL
// ─────────────────────────────────────────────────────────────

@Composable
fun AppNavigation(
    notificationService: NotificationService,
    webSocketClient: GastoWebSocketClient?   // ← nullable
) {
    val listaGastos = remember { mutableStateListOf<Gasto>() }
    val alertas = remember { mutableStateListOf<String>() }
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    val presupuestos = remember {
        mapOf(
            "Comida"      to 500_000.0,
            "Transporte"  to 200_000.0,
            "Ocio"        to 300_000.0,
            "Salud"       to 150_000.0,
            "Otros"       to 250_000.0
        )
    }

    // Conectar WebSocket solo si está configurado
    LaunchedEffect(Unit) {
        webSocketClient?.let { ws ->
            try {
                ws.conectar()
                ws.onAlertaRecibida = { msg -> alertas.add(0, msg) }
            } catch (e: Exception) {
                // Silencioso: WebSocket no disponible
            }
        }
    }

    Scaffold(
        bottomBar = {
            BarraNavegacionInferior(tabSeleccionado) { tabSeleccionado = it }
        },
        containerColor = Color(0xFF0F0F13)
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tabSeleccionado) {
                0 -> PantallaInicio(
                    listaGastos = listaGastos,
                    presupuestos = presupuestos,
                    onAgregarGasto = { gasto ->
                        listaGastos.add(gasto)
                        val cat        = gasto.categoria
                        val presupuesto = presupuestos[cat] ?: return@PantallaInicio
                        val totalCat   = listaGastos.filter { it.categoria == cat }.sumOf { it.valor }
                        val pct        = GastoValidator.calcularPorcentaje(totalCat, presupuesto)
                        if (GastoValidator.superaUmbral(totalCat, presupuesto)) {
                            val msg = GastoValidator.mensajeAlerta(cat, pct)
                            notificationService.mostrarNotificacion("⚠️ Alerta de Gasto", msg)
                            alertas.add(0, msg)
                            webSocketClient?.enviarAlerta(cat, pct)
                        }
                    }
                )
                1 -> PantallaGraficas(listaGastos, presupuestos)
                2 -> PantallaAlertas(alertas)
                3 -> PantallaConfiguracion()
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PANTALLA INICIO
// ─────────────────────────────────────────────────────────────

@Composable
fun PantallaInicio(
    listaGastos: List<Gasto>,
    presupuestos: Map<String, Double>,
    onAgregarGasto: (Gasto) -> Unit
) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    val totalGastado     = listaGastos.sumOf { it.valor }
    val presupuestoTotal = presupuestos.values.sum()
    val formatter        = NumberFormat.getNumberInstance(Locale("es", "CO"))

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F13)),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { TarjetaResumen(totalGastado, presupuestoTotal, formatter) }

            item {
                Text("Por categoría", color = Color(0xFF888888),
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categorias = listaGastos.groupBy { it.categoria }
                    presupuestos.keys.take(3).forEach { cat ->
                        TarjetaCategoria(
                            categoria   = cat,
                            gastado     = categorias[cat]?.sumOf { it.valor } ?: 0.0,
                            presupuesto = presupuestos[cat] ?: 1.0,
                            modifier    = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                Text("Recientes", color = Color(0xFF888888),
                    fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            if (listaGastos.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(80.dp)
                            .clip(RoundedCornerShape(16.dp)).background(Color(0xFF1A1A24)),
                        contentAlignment = Alignment.Center
                    ) { Text("Sin gastos registrados", color = Color(0xFF555555), fontSize = 13.sp) }
                }
            }

            items(listaGastos.reversed()) { gasto ->
                TarjetaTransaccion(gasto, formatter)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        ExtendedFloatingActionButton(
            onClick = { mostrarFormulario = true },
            containerColor = Color(0xFF6C47FF),
            contentColor   = Color.White,
            modifier       = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Nuevo gasto")
        }
    }

    if (mostrarFormulario) {
        DialogAgregarGasto(
            onDismiss = { mostrarFormulario = false },
            onAgregar = { onAgregarGasto(it); mostrarFormulario = false },
            categorias = listOf("Comida", "Transporte", "Ocio", "Salud", "Otros")
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TARJETA RESUMEN
// ─────────────────────────────────────────────────────────────

@Composable
fun TarjetaResumen(totalGastado: Double, presupuestoTotal: Double, formatter: NumberFormat) {
    val porcentaje = if (presupuestoTotal > 0)
        (totalGastado / presupuestoTotal).coerceIn(0.0, 1.0) else 0.0
    val colorBarra = when {
        porcentaje < 0.6 -> Color(0xFF6BFFB8)
        porcentaje < 0.8 -> Color(0xFFFFD166)
        else             -> Color(0xFFFF6B6B)
    }
    Box(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF6C47FF), Color(0xFF4F8AFF))))
            .padding(20.dp)
    ) {
        Column {
            Text("Gasto total del mes", color = Color(0xAAFFFFFF), fontSize = 12.sp)
            Spacer(Modifier.height(6.dp))
            Text("$${formatter.format(totalGastado)} COP",
                color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Presupuesto: $${formatter.format(presupuestoTotal)} · ${(porcentaje * 100).toInt()}% usado",
                color = Color(0xAAFFFFFF), fontSize = 11.sp)
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress    = { porcentaje.toFloat() },
                modifier    = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color       = colorBarra,
                trackColor  = Color(0x33FFFFFF)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// TARJETA CATEGORÍA
// ─────────────────────────────────────────────────────────────

@Composable
fun TarjetaCategoria(
    categoria: String, gastado: Double, presupuesto: Double, modifier: Modifier = Modifier
) {
    val emojis = mapOf("Comida" to "🍔","Transporte" to "🚌","Ocio" to "🎮","Salud" to "🏥","Otros" to "📦")
    val pct   = GastoValidator.calcularPorcentaje(gastado, presupuesto)
    val color = if (pct >= 0.8) Color(0xFFFF6B6B) else Color(0xFF6C47FF)

    Column(
        modifier = modifier.clip(RoundedCornerShape(18.dp))
            .background(Color(0xFF1A1A24)).padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(emojis[categoria] ?: "💳", fontSize = 20.sp)
        Text(categoria, fontSize = 9.sp, color = Color(0xFF888888))
        Text("$${(gastado / 1000).toInt()}K",
            fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFFE0E0E0))
        LinearProgressIndicator(
            progress   = { pct.toFloat() },
            modifier   = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)),
            color      = color,
            trackColor = Color(0x33FFFFFF)
        )
    }
}

// ─────────────────────────────────────────────────────────────
// TARJETA TRANSACCIÓN
// ─────────────────────────────────────────────────────────────

@Composable
fun TarjetaTransaccion(gasto: Gasto, formatter: NumberFormat) {
    val info = mapOf(
        "Comida"     to Pair("🍔", Color(0x33FF6B6B)),
        "Transporte" to Pair("🚌", Color(0x336C47FF)),
        "Ocio"       to Pair("🎮", Color(0x33FFD166)),
        "Salud"      to Pair("🏥", Color(0x336BFFB8)),
        "Otros"      to Pair("📦", Color(0x33888888))
    )
    val (ico, bg) = info[gasto.categoria] ?: Pair("💳", Color(0x33888888))

    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1A1A24)).padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(modifier = Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(bg),
            contentAlignment = Alignment.Center) { Text(ico, fontSize = 16.sp) }
        Column(modifier = Modifier.weight(1f)) {
            Text(gasto.nombre, color = Color(0xFFE0E0E0), fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text(gasto.categoria, color = Color(0xFF666666), fontSize = 10.sp)
        }
        Text("-$${formatter.format(gasto.valor)}",
            color = Color(0xFFFF6B6B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ─────────────────────────────────────────────────────────────
// DIÁLOGO AGREGAR GASTO
// ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogAgregarGasto(onDismiss: () -> Unit, onAgregar: (Gasto) -> Unit, categorias: List<String>) {
    var nombre               by remember { mutableStateOf("") }
    var valor                by remember { mutableStateOf("") }
    var categoriaSeleccionada by remember { mutableStateOf(categorias.first()) }
    var expandido            by remember { mutableStateOf(false) }
    var errorMensaje         by remember { mutableStateOf("") }

    val coloresTextField = OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = Color(0xFF6C47FF),
        unfocusedBorderColor = Color(0xFF333344),
        focusedTextColor     = Color.White,
        unfocusedTextColor   = Color.White,
        cursorColor          = Color(0xFF6C47FF),
        focusedLabelColor    = Color(0xFF9B7EFF),
        unfocusedLabelColor  = Color(0xFF888888)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color(0xFF1A1A24),
        title = { Text("Nuevo gasto", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (errorMensaje.isNotEmpty())
                    Text(errorMensaje, color = Color(0xFFFF6B6B), fontSize = 12.sp)

                OutlinedTextField(value = nombre, onValueChange = { nombre = it; errorMensaje = "" },
                    label = { Text("Descripción") }, singleLine = true,
                    colors = coloresTextField, modifier = Modifier.fillMaxWidth())

                OutlinedTextField(value = valor, onValueChange = { valor = it; errorMensaje = "" },
                    label = { Text("Valor (COP)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, colors = coloresTextField, modifier = Modifier.fillMaxWidth())

                ExposedDropdownMenuBox(expanded = expandido, onExpandedChange = { expandido = !expandido }) {
                    OutlinedTextField(
                        value = categoriaSeleccionada, onValueChange = {}, readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                        colors = coloresTextField, modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandido, onDismissRequest = { expandido = false },
                        containerColor = Color(0xFF252532)) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat, color = Color.White) },
                                onClick = { categoriaSeleccionada = cat; expandido = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val v = valor.replace(".", "").replace(",", ".").toDoubleOrNull()
                    if (!GastoValidator.esValido(nombre, v)) {
                        errorMensaje = "Completa todos los campos correctamente"
                    } else {
                        onAgregar(Gasto(nombre.trim(), v!!, categoriaSeleccionada))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6C47FF))
            ) { Text("Agregar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color(0xFF888888)) }
        }
    )
}

// ─────────────────────────────────────────────────────────────
// PANTALLA GRÁFICAS
// ─────────────────────────────────────────────────────────────

@Composable
fun PantallaGraficas(listaGastos: List<Gasto>, presupuestos: Map<String, Double>) {
    val formatter    = NumberFormat.getNumberInstance(Locale("es", "CO"))
    val porCategoria = listaGastos.groupBy { it.categoria }.mapValues { (_, g) -> g.sumOf { it.valor } }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F13)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Análisis por categoría", color = Color.White,
                fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp))
        }
        items(presupuestos.entries.toList()) { (cat, presupuesto) ->
            val gastado = porCategoria[cat] ?: 0.0
            val pct     = GastoValidator.calcularPorcentaje(gastado, presupuesto)
            val color   = when { pct < 0.6 -> Color(0xFF6BFFB8); pct < 0.8 -> Color(0xFFFFD166); else -> Color(0xFFFF6B6B) }
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF1A1A24)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(cat, color = Color(0xFFE0E0E0), fontWeight = FontWeight.Medium)
                    Text("$${formatter.format(gastado)} / $${formatter.format(presupuesto)}",
                        color = Color(0xFF888888), fontSize = 12.sp)
                }
                LinearProgressIndicator(progress = { pct.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = color, trackColor = Color(0x33FFFFFF))
                Text("${(pct * 100).toInt()}% del presupuesto", color = color, fontSize = 11.sp)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PANTALLA ALERTAS
// ─────────────────────────────────────────────────────────────

@Composable
fun PantallaAlertas(alertas: List<String>) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F13)).padding(16.dp)) {
        Text("Notificaciones", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp))
        if (alertas.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔔", fontSize = 48.sp)
                    Spacer(Modifier.height(12.dp))
                    Text("Sin alertas por ahora", color = Color(0xFF666666))
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(alertas) { alerta ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1A1A24)).padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(0xFF6C47FF)))
                        Text(alerta, color = Color(0xFFE0E0E0), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// PANTALLA CONFIGURACIÓN
// ─────────────────────────────────────────────────────────────

@Composable
fun PantallaConfiguracion() {
    var notifComida     by remember { mutableStateOf(true) }
    var notifTransporte by remember { mutableStateOf(true) }
    var notifOcio       by remember { mutableStateOf(false) }
    var umbral          by remember { mutableStateOf(80f) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F13)).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Configuración", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp))
        }
        item {
            Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1A1A24)).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Umbral de alerta", color = Color(0xFF888888), fontSize = 12.sp)
                Text("${umbral.toInt()}%", color = Color.White, fontWeight = FontWeight.Bold)
                Slider(value = umbral, onValueChange = { umbral = it },
                    valueRange = 50f..95f, steps = 8,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF6C47FF), activeTrackColor = Color(0xFF6C47FF)))
            }
        }
        item {
            Text("Notificaciones por categoría", color = Color(0xFF888888),
                fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        item { FilaSwitch("Comida 🍔",     notifComida)     { notifComida = it } }
        item { FilaSwitch("Transporte 🚌", notifTransporte) { notifTransporte = it } }
        item { FilaSwitch("Ocio 🎮",       notifOcio)       { notifOcio = it } }
    }
}

@Composable
fun FilaSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
        .background(Color(0xFF1A1A24)).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(label, color = Color(0xFFE0E0E0), modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = Color(0xFF6C47FF)))
    }
}

// ─────────────────────────────────────────────────────────────
// BARRA DE NAVEGACIÓN INFERIOR
// ─────────────────────────────────────────────────────────────

@Composable
fun BarraNavegacionInferior(tabSeleccionado: Int, onTabChange: (Int) -> Unit) {
    NavigationBar(containerColor = Color(0xFF0F0F13)) {
        listOf(
            Triple("Inicio",   Icons.Default.Home,          0),
            Triple("Gráficas", Icons.Default.PieChart,      1),
            Triple("Alertas",  Icons.Default.Notifications, 2),
            Triple("Config.",  Icons.Default.Settings,      3)
        ).forEach { (label, icon, idx) ->
            NavigationBarItem(
                selected = tabSeleccionado == idx,
                onClick  = { onTabChange(idx) },
                icon     = { Icon(icon, contentDescription = label) },
                label    = { Text(label, fontSize = 10.sp) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = Color(0xFF6C47FF),
                    selectedTextColor   = Color(0xFF6C47FF),
                    unselectedIconColor = Color(0xFF555555),
                    unselectedTextColor = Color(0xFF555555),
                    indicatorColor      = Color(0x226C47FF)
                )
            )
        }
    }
}