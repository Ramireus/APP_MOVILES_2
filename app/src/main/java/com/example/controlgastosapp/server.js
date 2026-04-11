/**
 * Servidor WebSocket para Control de Gastos App
 * Sprint 3 — HU-04: Notificaciones Push en Tiempo Real
 *
 * Tecnología: ws (biblioteca WebSocket para Node.js)
 * Exposición pública: NGROK (ngrok http 3000)
 *
 * Instalación:
 *   npm install ws
 *   node server.js
 *   ngrok http 3000   → copiar URL en GastoWebSocketClient.kt
 *
 * Flujo:
 *   App Android → GASTO_UMBRAL → Este servidor → ALERTA → Todos los clientes
 */

const WebSocket = require('ws');

const PUERTO = 3000;
const server = new WebSocket.Server({ port: PUERTO });

let clientes = new Set();

console.log(`\n🚀 Servidor WebSocket iniciado en puerto ${PUERTO}`);
console.log(`📡 Exponer con: ngrok http ${PUERTO}`);
console.log(`⏳ Esperando conexiones...\n`);

server.on('connection', (ws, req) => {
  const ip = req.socket.remoteAddress;
  console.log(`✅ Cliente conectado desde: ${ip}`);
  clientes.add(ws);

  // Enviar confirmación al cliente recién conectado
  ws.send(JSON.stringify({
    tipo: 'BIENVENIDA',
    mensaje: 'Conectado al servidor de alertas',
    timestamp: Date.now()
  }));

  ws.on('message', (data) => {
    try {
      const mensaje = JSON.parse(data.toString());
      console.log(`📩 Recibido [${ip}]:`, mensaje);

      // Procesar evento de umbral superado
      if (mensaje.tipo === 'GASTO_UMBRAL') {
        const { categoria, porcentaje } = mensaje;
        const textoAlerta = `⚠️ Superaste el ${porcentaje}% del presupuesto en ${categoria}`;

        console.log(`🔔 Generando alerta: ${textoAlerta}`);

        // Difundir la alerta a TODOS los clientes conectados
        const respuesta = JSON.stringify({
          tipo: 'ALERTA',
          mensaje: textoAlerta,
          categoria,
          porcentaje,
          timestamp: Date.now()
        });

        clientes.forEach((cliente) => {
          if (cliente.readyState === WebSocket.OPEN) {
            cliente.send(respuesta);
            console.log(`  → Alerta enviada a cliente`);
          }
        });
      }

    } catch (error) {
      console.error('❌ Error al parsear mensaje:', error.message);
    }
  });

  ws.on('close', () => {
    clientes.delete(ws);
    console.log(`🔌 Cliente desconectado: ${ip} | Clientes activos: ${clientes.size}`);
  });

  ws.on('error', (err) => {
    console.error(`❌ Error de cliente [${ip}]:`, err.message);
    clientes.delete(ws);
  });
});

// Ping periódico para mantener conexiones vivas (NGROK cierra inactivas)
setInterval(() => {
  clientes.forEach((ws) => {
    if (ws.readyState === WebSocket.OPEN) {
      ws.ping();
    }
  });
}, 30_000);

console.log('💡 Tip: usa wscat -c ws://localhost:3000 para probar la conexión');
