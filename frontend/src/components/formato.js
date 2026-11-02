const ENTEROS = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 });
const DECIMALES = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 2 });

export const pesos = (valor) => `$ ${ENTEROS.format(Math.round(Number(valor) || 0))}`;
export const pesosConDecimales = (valor) => `$ ${DECIMALES.format(Number(valor) || 0)}`;
export const numero = (valor) => DECIMALES.format(Number(valor) || 0);
export const unidad = (codigo) => (codigo === 'UNIDAD' ? 'u.' : 'ml');
export const cantidad = (valor, codigoUnidad) => `${numero(valor)} ${unidad(codigoUnidad)}`;
export const conSigno = (valor) => `${Number(valor) > 0 ? '+' : ''}${numero(valor)}`;

export function hora(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleTimeString('es-AR', { hour: '2-digit', minute: '2-digit', hour12: false });
}

export function fechaHora(iso) {
  if (!iso) return '';
  return new Date(iso).toLocaleString('es-AR', { day: '2-digit', month: '2-digit', hour: '2-digit', minute: '2-digit', hour12: false });
}

/** "19:30:00" → "19:30" */
export const horaCorta = (texto) => (texto ? String(texto).slice(0, 5) : '');

export const ingredientesTexto = (ingredientes = []) =>
  ingredientes.map((i) => `${numero(i.cantidad)} ${unidad(i.unidadMedida)} ${i.insumo}`).join(' · ');
