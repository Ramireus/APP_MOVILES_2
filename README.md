# Control de Gastos App

Aplicación móvil desarrollada en Android Studio con Kotlin y Jetpack Compose para registrar, categorizar y controlar gastos personales con notificaciones push en tiempo real.

---

## Historias de Usuario

| ID | Historia | Sprint |
|----|----------|--------|
| HU-01 | Como usuario quiero registrar un gasto con nombre, valor y categoría para llevar un control de mis finanzas | Sprint 1 |
| HU-02 | Como usuario quiero ver el total acumulado de mis gastos para saber cuánto he gastado | Sprint 1 |
| HU-03 | Como usuario quiero ver mis gastos organizados por categoría con barras de progreso para identificar en qué gasto más | Sprint 2–3 |
| HU-04 | Como usuario quiero recibir notificaciones push cuando supero el 80% del presupuesto por categoría para tomar decisiones a tiempo | Sprint 3 |

---

## Sprint 1

### Funcionalidades
- Registro de gastos en memoria (nombre, valor, categoría)
- Cálculo del total acumulado
- Validación de datos: nombre no vacío, valor positivo

### Pruebas realizadas
- Inserción de datos correcta
- Validación de valores negativos → rechazados
- Cálculo del total acumulado

---

## Sprint 2

### Funcionalidades
- Interfaz gráfica con Jetpack Compose
- Registro dinámico de gastos desde formulario
- Visualización del total en tiempo real
- Lista de gastos en pantalla

### Pruebas realizadas
- Registro desde interfaz
- Validación de datos inválidos
- Actualización automática del total
- Visualización correcta de los gastos

---

## Sprint 3

### Funcionalidades
- Rediseño completo de interfaz (tema oscuro, gradientes, tarjetas)
- Dashboard con resumen mensual y barra de progreso por presupuesto
- Navegación inferior con 4 pantallas: Inicio, Gráficas, Alertas, Configuración
- Barras de progreso por categoría con cambio de color según nivel de gasto
- Notificaciones push locales al superar el umbral de presupuesto (HU-04)
- Comunicación en tiempo real mediante WebSocket (OkHttp) + servidor Node.js
- Exposición del servidor local con NGROK para pruebas en dispositivo real
- Pantalla de alertas con historial de notificaciones recibidas
- Configuración de umbral de alerta (50%–95%) y switches por categoría

### Arquitectura de notificaciones

```
App Android → supera umbral → WebSocket → Servidor Node.js
                                               ↓
                                      Difunde ALERTA
                                               ↓
                              App recibe → Notificación push local
```

### Pruebas realizadas (9 pruebas unitarias — GastoUnitTest.kt)

| Prueba | Resultado |
|--------|-----------|
| Gasto válido es aceptado | ✅ PASS |
| Nombre vacío es rechazado | ✅ PASS |
| Valor negativo es rechazado | ✅ PASS |
| Valor cero es rechazado | ✅ PASS |
| Suma total es correcta | ✅ PASS |
| Agrupación por categoría es correcta | ✅ PASS |
| Porcentaje se calcula correctamente | ✅ PASS |
| Umbral 80% activa notificación | ✅ PASS |
| Umbral 79% no activa notificación | ✅ PASS |

---

## Tecnologías usadas

| Tecnología | Uso |
|------------|-----|
| Kotlin | Lenguaje principal |
| Android Studio | IDE de desarrollo |
| Jetpack Compose | Interfaz de usuario |
| OkHttp | Cliente WebSocket |
| Node.js + ws | Servidor WebSocket local |
| NGROK | Túnel para exponer el servidor |
| JUnit 4 | Pruebas unitarias |

---


## Configuración del servidor WebSocket (Sprint 3)

```bash
# 1. Instalar dependencias
cd server
npm install ws

# 2. Iniciar servidor
node server.js

# 3. Exponer con NGROK
ngrok http 3000

# 4. Copiar URL generada (ej: wss://abc123.ngrok-free.app/ws)
#    y pegarla en MainActivity.kt → val wsUrl = "..."
```

---

## Uso de la aplicación

1. Abrir la app → ver el dashboard con resumen del mes
2. Tocar **"Nuevo gasto"** → ingresar descripción, valor y categoría
3. Ver el gasto en la lista y las barras de progreso actualizadas
4. Al superar el 80% del presupuesto de una categoría → notificación push automática
5. Consultar el historial de alertas en la pestaña **Alertas**
6. Ajustar el umbral de notificación en **Configuración**

---

## Estado del proyecto

| Sprint | Estado |
|--------|--------|
| Sprint 1 | ✅ Completado |
| Sprint 2 | ✅ Completado |
| Sprint 3 | ✅ Completado — Examen Parcial |
