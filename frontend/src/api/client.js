// Único punto de contacto del frontend con el backend (/api). La vista no contiene reglas de negocio.

export class ErrorApi extends Error {
  constructor(mensaje, estado, detalle) {
    super(mensaje);
    this.estado = estado;
    this.detalle = detalle;
  }
}

async function pedir(metodo, ruta, cuerpo, signal) {
  let respuesta;
  try {
    respuesta = await fetch(`/api${ruta}`, {
      method: metodo,
      headers: cuerpo === undefined ? undefined : { 'Content-Type': 'application/json' },
      body: cuerpo === undefined ? undefined : JSON.stringify(cuerpo),
      signal,
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ErrorApi('No hay conexión con la API', 0);
  }
  const texto = await respuesta.text();
  let datos = null;
  try {
    datos = texto ? JSON.parse(texto) : null;
  } catch {
    datos = null;
  }
  if (!respuesta.ok) {
    let mensaje = datos?.detail || `La API respondió HTTP ${respuesta.status}`;
    if (datos?.errores) {
      mensaje += ': ' + Object.entries(datos.errores).map(([campo, error]) => `${campo} ${error}`).join(' · ');
    }
    throw new ErrorApi(mensaje, respuesta.status, datos);
  }
  return datos;
}

const bar = (id) => `/establecimientos/${id}`;

export const api = {
  async estado(signal) {
    const [aplicacion, salud] = await Promise.all([pedir('GET', '', undefined, signal), pedir('GET', '/health', undefined, signal)]);
    return { ...aplicacion, salud: salud.status };
  },

  establecimientos: {
    listar: () => pedir('GET', '/establecimientos'),
    crear: (datos) => pedir('POST', '/establecimientos', datos),
    editar: (id, datos) => pedir('PUT', bar(id), datos),
    configurarPrecios: (id, politica) => pedir('PUT', `${bar(id)}/politica-precio`, politica),
    configurarAlertas: (id, alertas) => pedir('PUT', `${bar(id)}/alertas`, alertas),
  },

  carta: (id) => pedir('GET', `${bar(id)}/carta`),
  compararPrecios: (id) => pedir('GET', `${bar(id)}/precios/comparacion`),

  insumos: {
    listar: (id, activo = true) => pedir('GET', `${bar(id)}/insumos?activo=${activo}`),
    crear: (id, datos) => pedir('POST', `${bar(id)}/insumos`, datos),
    editar: (id, insumoId, datos) => pedir('PUT', `${bar(id)}/insumos/${insumoId}`, datos),
    darDeBaja: (id, insumoId) => pedir('DELETE', `${bar(id)}/insumos/${insumoId}`),
    darDeAlta: (id, insumoId) => pedir('POST', `${bar(id)}/insumos/${insumoId}/alta`),
  },

  comprar: (id, insumoId, envases) => pedir('POST', `${bar(id)}/compras`, { insumoId, envases }),
  movimientos: (id, limite = 12) => pedir('GET', `${bar(id)}/movimientos?limite=${limite}`),

  recetas: {
    listar: (id, incluirFueraDelMenu = false) => pedir('GET', `${bar(id)}/recetas?incluirFueraDelMenu=${incluirFueraDelMenu}`),
    crear: (id, datos) => pedir('POST', `${bar(id)}/recetas`, datos),
    editar: (id, recetaId, datos) => pedir('PUT', `${bar(id)}/recetas/${recetaId}`, datos),
    quitarDelMenu: (id, recetaId) => pedir('DELETE', `${bar(id)}/recetas/${recetaId}`),
    volverAlMenu: (id, recetaId) => pedir('POST', `${bar(id)}/recetas/${recetaId}/alta`),
  },

  comandas: {
    registrar: (id, pedido) => pedir('POST', `${bar(id)}/comandas`, pedido),
    enCurso: (id) => pedir('GET', `${bar(id)}/comandas?enCurso=true`),
    recientes: (id, limite = 15) => pedir('GET', `${bar(id)}/comandas?limite=${limite}`),
    consultar: (id, comandaId) => pedir('GET', `${bar(id)}/comandas/${comandaId}`),
    preparar: (id, comandaId) => pedir('POST', `${bar(id)}/comandas/${comandaId}/preparar`),
    despachar: (id, comandaId) => pedir('POST', `${bar(id)}/comandas/${comandaId}/despachar`),
    anular: (id, comandaId, motivo) => pedir('POST', `${bar(id)}/comandas/${comandaId}/anular`, { motivo }),
  },

  auditorias: {
    registrar: (id, responsable, conteos) => pedir('POST', `${bar(id)}/auditorias`, { responsable, conteos }),
    listar: (id) => pedir('GET', `${bar(id)}/auditorias`),
  },

  reporte: (id, secciones = []) =>
    pedir('GET', `${bar(id)}/reportes${secciones.length ? `?secciones=${secciones.join(',')}` : ''}`),
  notificaciones: (id, limite = 20) => pedir('GET', `${bar(id)}/notificaciones?limite=${limite}`),
  traza: (id) => pedir('GET', `${bar(id)}/traza`),
};
