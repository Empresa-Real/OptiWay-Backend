const estado = document.getElementById('estado');

async function cargar(url, id, formatear) {
  const destino = document.getElementById(id);
  try {
    const respuesta = await fetch(url);
    const texto = await respuesta.text();
    let datos = texto;
    try { datos = JSON.parse(texto); } catch (error) { /* texto plano */ }
    destino.textContent = respuesta.ok ? formatear(datos) : (respuesta.status + ' - ' + texto);
  } catch (error) {
    destino.textContent = 'Error: ' + error.message;
  }
}

function actualizar() {
  estado.textContent = 'Actualizando...';
  Promise.all([
    cargar('/actuator/health', 'health', datos => datos.status || JSON.stringify(datos)),
    cargar('/api/estadisticas', 'estadisticas', datos => JSON.stringify(datos, null, 2)),
    cargar('/admin/metrics', 'metrics', datos => JSON.stringify(datos, null, 2)),
    cargar('/admin/logs?lines=100', 'logs', datos =>
      (datos.lineas || []).join('\n') || 'Sin líneas disponibles')
  ]).then(() => { estado.textContent = 'Listo'; });
}

document.getElementById('actualizar').addEventListener('click', actualizar);
actualizar();
