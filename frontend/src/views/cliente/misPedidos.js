// Comodidad por navegador: recuerda qué comandas pidió este cliente para poder seguirlas.
const clave = (establecimientoId) => `barstock_mis_pedidos_${establecimientoId}`;

export function leerMisPedidos(establecimientoId) {
  try {
    return JSON.parse(localStorage.getItem(clave(establecimientoId))) || [];
  } catch {
    return [];
  }
}

export function guardarMisPedidos(establecimientoId, ids) {
  try {
    localStorage.setItem(clave(establecimientoId), JSON.stringify(ids.slice(0, 20)));
  } catch {
    // Sin almacenamiento el seguimiento dura hasta recargar la página.
  }
}

export function recordarPedido(establecimientoId, comandaId) {
  guardarMisPedidos(establecimientoId, [comandaId, ...leerMisPedidos(establecimientoId).filter((id) => id !== comandaId)]);
}
