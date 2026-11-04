const ENTEROS = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 0 });
const DECIMALES = new Intl.NumberFormat('es-AR', { maximumFractionDigits: 2 });

export const pesos = (valor) => `$ ${ENTEROS.format(Math.round(Number(valor) || 0))}`;
export const pesosConDecimales = (valor) => `$ ${DECIMALES.format(Number(valor) || 0)}`;
export const numero = (valor) => DECIMALES.format(Number(valor) || 0);
export const unidad = (codigo) => (codigo === 'UNIDAD' ? 'u.' : 'ml');
export const cantidad = (valor, codigoUnidad) => `${numero(valor)} ${unidad(codigoUnidad)}`;
export const conSigno = (valor) => `${Number(valor) > 0 ? '+' : ''}${numero(valor)}`;

/** Lo que se mide en ml viene en botellas; lo que se cuenta por unidad, en envases. */
export const nombreEnvase = (codigoUnidad, plural = true) => (codigoUnidad === 'UNIDAD'
  ? (plural ? 'envases' : 'envase') : (plural ? 'botellas' : 'botella'));

export const envasesCerrados = (codigoUnidad) => (codigoUnidad === 'UNIDAD' ? 'Envases cerrados' : 'Botellas cerradas');

/** 970 ml en botellas de 750 → "1 botella + 220 ml"; 1500 → "2 botellas". */
export function enEnvases(valor, contenido, codigoUnidad) {
  const total = Math.max(0, Math.round(Number(valor) || 0));
  const tamano = Math.max(1, Number(contenido) || 1);
  const completos = Math.floor(total / tamano);
  const resto = total % tamano;
  const texto = `${completos} ${nombreEnvase(codigoUnidad, completos !== 1)}`;
  return resto ? `${texto} + ${cantidad(resto, codigoUnidad)}` : texto;
}

/** Cantidad expresada en envases con decimales: 20 ml de una botella de 750 → "0,03 botellas". */
export const envasesDecimal = (valor, contenido, codigoUnidad) => {
  const envases = (Number(valor) || 0) / Math.max(1, Number(contenido) || 1);
  return `${DECIMALES.format(envases)} ${nombreEnvase(codigoUnidad, Math.abs(envases) !== 1)}`;
};

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

// Jornadas: fechas "AAAA-MM-DD" sin hora. Se arman a mano para no correrlas de día por la zona horaria.
const DIAS = ['dom', 'lun', 'mar', 'mié', 'jue', 'vie', 'sáb'];
const dos = (n) => String(n).padStart(2, '0');
const aFecha = (iso) => {
  const [anio, mes, dia] = String(iso).split('-').map(Number);
  return new Date(anio, mes - 1, dia);
};
const aIso = (fecha) => `${fecha.getFullYear()}-${dos(fecha.getMonth() + 1)}-${dos(fecha.getDate())}`;

/** "2026-11-02" → "02/11" */
export const diaMes = (iso) => {
  const fecha = aFecha(iso);
  return `${dos(fecha.getDate())}/${dos(fecha.getMonth() + 1)}`;
};

/** "2026-11-02" → "lun 02/11" */
export const diaSemana = (iso) => `${DIAS[aFecha(iso).getDay()]} ${diaMes(iso)}`;

export const sumarDias = (iso, dias) => {
  const fecha = aFecha(iso);
  fecha.setDate(fecha.getDate() + dias);
  return aIso(fecha);
};

/** La jornada va de 6:00 a 5:59: antes de las 6 todavía es la del día anterior. */
export const jornadaActual = () => {
  const ahora = new Date();
  ahora.setHours(ahora.getHours() - 6);
  return aIso(ahora);
};

export const cantidadJornadas = (periodo) => {
  const dias = Math.round((aFecha(periodo.hasta) - aFecha(periodo.desde)) / 86400000) + 1;
  return dias === 1 ? '1 jornada' : `${dias} jornadas`;
};

export const periodoTexto = (periodo) => (periodo.desde === periodo.hasta
  ? diaSemana(periodo.desde) : `${diaMes(periodo.desde)} al ${diaMes(periodo.hasta)}`);

export const variacionTexto = (valor) => (valor === null || valor === undefined
  ? 'sin ventas para comparar' : `${Number(valor) > 0 ? '+' : ''}${DECIMALES.format(Number(valor))} %`);

export const horario = (turno, horarioBar) => (turno === 'MEDIODIA'
  ? `${horaCorta(horarioBar.mediodiaApertura)} a ${horaCorta(horarioBar.mediodiaCierre)}`
  : `${horaCorta(horarioBar.nocheApertura)} a ${horaCorta(horarioBar.nocheCierre)}`);
