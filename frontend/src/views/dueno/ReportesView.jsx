import { useState } from 'react';
import { api } from '../../api/client.js';
import { cantidadJornadas, diaMes, diaSemana, horario, jornadaActual, numero, periodoTexto, pesos, sumarDias } from '../../components/formato.js';
import { Cargando, Encabezado, Kpi, Tarjeta } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';
import { Ranking, SeccionReporte } from './secciones.jsx';

const SECCIONES = [
  ['GRAFICO', 'Ranking de tragos'],
  ['TENDENCIAS', 'Tendencia de ventas'],
  ['MERMAS', 'Mermas por insumo'],
  ['MARGEN', 'Margen por trago'],
];

const PERIODOS = [
  ['hoy', 'Jornada actual'],
  ['ayer', 'Jornada anterior'],
  ['7', 'Últimos 7 días'],
  ['30', 'Últimos 30 días'],
  ['otro', 'Elegir fechas'],
];

const TURNOS = { MEDIODIA: 'Mediodía', NOCHE: 'Noche' };

function rango(periodo, desde, hasta) {
  const hoy = jornadaActual();
  if (periodo === 'hoy') return { desde: hoy, hasta: hoy };
  if (periodo === 'ayer') return { desde: sumarDias(hoy, -1), hasta: sumarDias(hoy, -1) };
  if (periodo === 'otro') return { desde, hasta };
  return { desde: sumarDias(hoy, -(Number(periodo) - 1)), hasta: hoy };
}

function etiquetaTurno(turno, bares) {
  return bares.length === 1 ? `${TURNOS[turno]} (${horario(turno, bares[0].horario)})` : `${TURNOS[turno]} (según el horario de cada bar)`;
}

function Indicadores({ indicadores: i }) {
  return (
    <div className="cols-4">
      <Kpi etiqueta="Ventas netas" valor={pesos(i.ventasNetas)} detalle={`${i.comandasCobradas} comandas · ticket ${pesos(i.ticketPromedio)}`} />
      <Kpi etiqueta="Contribución por tragos" valor={pesos(i.contribucion)} tono={Number(i.contribucion) < 0 ? 'warn' : 'accent'}
           detalle={i.margenContribucion !== null ? `Margen ${numero(i.margenContribucion)} % sobre ventas` : 'Ventas menos insumos consumidos'} />
      <Kpi etiqueta="Compras registradas" valor={pesos(i.compras)} detalle="Botellas que entraron al stock" />
      <Kpi etiqueta="Pérdidas por merma" valor={pesos(i.mermas)} tono="warn" detalle={`${i.auditorias} recuento(s) en el período`} />
    </div>
  );
}

function ResumenTurno({ titulo, subtitulo, indicadores: i, destacado }) {
  const filas = [
    ['Ventas netas', pesos(i.ventasNetas)],
    ['Comandas cobradas', i.comandasCobradas],
    ['Tragos vendidos', i.unidadesVendidas],
    ['Ticket promedio', pesos(i.ticketPromedio)],
    ['Tragos devueltos', `${i.tragosDevueltos} (${pesos(i.devoluciones)})`],
    ['Merma de los recuentos', pesos(i.mermas)],
  ];
  return (
    <Tarjeta titulo={titulo} subtitulo={subtitulo} className={destacado ? 'tarjeta-destacada' : ''}>
      {filas.map(([etiqueta, valor]) => (
        <div className="record" key={etiqueta}><span className="small">{etiqueta}</span><b>{valor}</b></div>
      ))}
    </Tarjeta>
  );
}

function ReporteDiario({ datos, bares }) {
  const siguiente = sumarDias(datos.jornada, 1);
  return (
    <>
      <div className="note info mt16">
        Jornada del <b>{diaSemana(datos.jornada)}</b>: desde las 6:00 del {diaMes(datos.jornada)} hasta las 5:59 del {diaMes(siguiente)}.
        Lo vendido después de medianoche se suma a la noche del {diaMes(datos.jornada)}. Lo que se registra fuera de horario (por ejemplo, el recuento después de cerrar) cuenta en el turno más cercano.
      </div>
      <div className="cols-3 mt16">
        {datos.turnos.map((t) => (
          <ResumenTurno key={t.codigo} titulo={`Turno ${t.nombre.toLowerCase()}`}
                        subtitulo={bares.length === 1 ? `De ${horario(t.codigo, bares[0].horario)}` : 'Según el horario de cada bar'}
                        indicadores={t.indicadores} />
        ))}
        <ResumenTurno titulo="Total de la jornada" subtitulo="Mediodía + noche" indicadores={datos.total} destacado />
      </div>
      <Tarjeta titulo="Tragos más vendidos de la jornada" subtitulo="Unidades vendidas, sin contar las devueltas" className="mt16">
        <Ranking datos={datos.ranking} />
      </Tarjeta>
    </>
  );
}

export default function ReportesView() {
  const { establecimientos } = useSesion();
  const [modo, setModo] = useState('periodo');
  const [bar, setBar] = useState('');
  const [periodo, setPeriodo] = useState('7');
  const [desde, setDesde] = useState(() => sumarDias(jornadaActual(), -6));
  const [hasta, setHasta] = useState(() => jornadaActual());
  const [turno, setTurno] = useState('');
  const [jornada, setJornada] = useState(() => jornadaActual());
  const [secciones, setSecciones] = useState(['GRAFICO', 'TENDENCIAS']);

  const ids = bar ? [bar] : [];
  const bares = bar ? establecimientos.filter((b) => b.id === bar) : establecimientos;
  const elegido = rango(periodo, desde, hasta);
  const reporte = useDatos(() => (modo === 'periodo'
    ? api.reportes.generar({ establecimientos: ids, desde: elegido.desde, hasta: elegido.hasta, turno, secciones })
    : null), [modo, bar, elegido.desde, elegido.hasta, turno, secciones.join(',')]);
  const diario = useDatos(() => (modo === 'diario' ? api.reportes.diario({ establecimientos: ids, jornada }) : null),
    [modo, bar, jornada]);

  const alternar = (tipo) => setSecciones((actuales) => (actuales.includes(tipo)
    ? actuales.filter((t) => t !== tipo) : [...actuales, tipo]));

  return (
    <>
      <Encabezado titulo="Reportes y rentabilidad"
                  descripcion="Ventas, costos, compras y mermas de tus bares. Elegí el establecimiento, las fechas y el turno; las secciones se suman al reporte en el orden en que las marcás." />
      <Tarjeta titulo="Qué querés ver"
               subtitulo="La jornada va de 6:00 a 5:59: lo vendido después de medianoche cuenta para el día en que abrió el turno."
               extra={<button className="btn btn-small" type="button" onClick={modo === 'periodo' ? reporte.recargar : diario.recargar}>↻ Actualizar</button>}>
        <div className="segmentado mb14" role="radiogroup" aria-label="Tipo de reporte">
          {[['periodo', 'Por período'], ['diario', 'Reporte diario']].map(([codigo, nombre]) => (
            <button key={codigo} type="button" role="radio" aria-checked={modo === codigo} className={modo === codigo ? 'activo' : ''}
                    onClick={() => setModo(codigo)}>{nombre}</button>
          ))}
        </div>
        <div className="filtros">
          <div className="field">
            <label htmlFor="fBar">Establecimiento</label>
            <select id="fBar" value={bar} onChange={(e) => setBar(e.target.value)}>
              <option value="">Todos los establecimientos</option>
              {establecimientos.map((b) => <option key={b.id} value={b.id}>{b.nombre}</option>)}
            </select>
          </div>
          {modo === 'periodo' ? (
            <>
              <div className="field">
                <label htmlFor="fPeriodo">Período</label>
                <select id="fPeriodo" value={periodo} onChange={(e) => setPeriodo(e.target.value)}>
                  {PERIODOS.map(([codigo, nombre]) => <option key={codigo} value={codigo}>{nombre}</option>)}
                </select>
              </div>
              {periodo === 'otro' && (
                <>
                  <div className="field">
                    <label htmlFor="fDesde">Desde la jornada</label>
                    <input id="fDesde" type="date" value={desde} max={hasta} onChange={(e) => e.target.value && setDesde(e.target.value)} />
                  </div>
                  <div className="field">
                    <label htmlFor="fHasta">Hasta la jornada</label>
                    <input id="fHasta" type="date" value={hasta} min={desde} max={jornadaActual()} onChange={(e) => e.target.value && setHasta(e.target.value)} />
                  </div>
                </>
              )}
              <div className="field">
                <label htmlFor="fTurno">Turno</label>
                <select id="fTurno" value={turno} onChange={(e) => setTurno(e.target.value)}>
                  <option value="">Mediodía y noche</option>
                  {Object.keys(TURNOS).map((t) => <option key={t} value={t}>{etiquetaTurno(t, bares)}</option>)}
                </select>
              </div>
            </>
          ) : (
            <div className="field">
              <label htmlFor="fJornada">Jornada</label>
              <input id="fJornada" type="date" value={jornada} max={jornadaActual()} onChange={(e) => e.target.value && setJornada(e.target.value)} />
            </div>
          )}
        </div>
        {modo === 'periodo' && (
          <>
            <div className="small strong mt16 mb8">Secciones</div>
            <div className="casillas">
              {SECCIONES.map(([tipo, nombre]) => (
                <label key={tipo} className={`casilla${secciones.includes(tipo) ? ' activa' : ''}`}>
                  <input type="checkbox" checked={secciones.includes(tipo)} onChange={() => alternar(tipo)} /> {nombre}
                </label>
              ))}
            </div>
          </>
        )}
      </Tarjeta>

      {modo === 'periodo' ? (
        <Cargando estado={reporte}>
          {(datos) => {
            const i = datos.indicadores;
            return (
              <>
                <div className="note mt16">
                  <b>{periodoTexto(datos.periodo)}</b> ({cantidadJornadas(datos.periodo)})
                  {' · '}{datos.turno ? `Turno ${TURNOS[datos.turno].toLowerCase()}` : 'Mediodía y noche'}
                  {' · '}{datos.establecimientos.join(', ')}
                </div>
                <div className="mt16"><Indicadores indicadores={i} /></div>
                <div className="cols-half mt16">
                  {datos.secciones.map((s) => <SeccionReporte key={s.tipo} seccion={s} />)}
                </div>
                <Tarjeta titulo="Cómo se calcula" subtitulo="Cifras orientativas" className="mt16">
                  <div className="flow">
                    <div className="secondary-stat"><div className="kpi-label">Costo de insumos consumidos</div><strong>{pesos(i.costoInsumos)}</strong><div className="tiny muted">Incluye los tragos que después se devolvieron.</div></div>
                    <div className="secondary-stat"><div className="kpi-label">Devoluciones</div><strong>{pesos(i.devoluciones)}</strong><div className="tiny muted">{i.tragosDevueltos} trago(s) devuelto(s); los insumos no vuelven al stock.</div></div>
                    <div className="secondary-stat"><div className="kpi-label">Tragos vendidos</div><strong>{i.unidadesVendidas}</strong><div className="tiny muted">De comandas cobradas, sin los devueltos.</div></div>
                  </div>
                  <div className="note mt16">No incluye sueldos, alquiler, impuestos ni otros gastos del local: la contribución no es la ganancia final del negocio.</div>
                </Tarjeta>
              </>
            );
          }}
        </Cargando>
      ) : (
        <Cargando estado={diario}>{(datos) => <ReporteDiario datos={datos} bares={bares} />}</Cargando>
      )}
    </>
  );
}
