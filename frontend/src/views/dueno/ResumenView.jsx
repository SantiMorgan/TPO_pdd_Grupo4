import { api } from '../../api/client.js';
import { enEnvases, horario, pesos } from '../../components/formato.js';
import { Kpi, Pill, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

export default function ResumenView({ ir }) {
  const { establecimientos, establecimientoId, cambiarEstablecimiento } = useSesion();
  // Las últimas 7 jornadas y el stock de cada bar: el dueño compara todos sus establecimientos.
  const resumen = useDatos(() => Promise.all(establecimientos.map(async (bar) => {
    const [reporte, insumos] = await Promise.all([api.reportes.generar({ establecimientos: [bar.id] }), api.insumos.listar(bar.id)]);
    return { bar, indicadores: reporte.indicadores, criticos: insumos.filter((i) => i.necesitaReposicion), insumos: insumos.length };
  })), [establecimientos.map((b) => b.id).join(',')], { intervalo: 15000 });
  const filas = resumen.datos || [];
  const suma = (campo) => filas.reduce((total, f) => total + Number(f.indicadores[campo]), 0);
  const criticos = filas.flatMap((f) => f.criticos.map((insumo) => ({ bar: f.bar, insumo })));

  return (
    <>
      <section className="hero">
        <div>
          <div className="eyebrow">Panel del dueño · {establecimientos.length} establecimiento(s)</div>
          <h1>Cada mililitro cuenta.<br /> Cada decisión también.</h1>
          <p>Del trago despachado al inventario real: recetas, trazabilidad, precios con Happy Hour y detección de mermas en todos tus bares.</p>
          <div className="flex gap8 wrap">
            <button className="btn btn-primary" type="button" onClick={() => ir('reportes')}>Ver reportes →</button>
            <button className="btn btn-dark" type="button" onClick={() => ir('precios')}>Configurar precios</button>
          </div>
        </div>
        <div className="hero-art">🍸</div>
      </section>

      <div className="cols-4">
        <Kpi etiqueta="Ventas netas · últimos 7 días" valor={pesos(suma('ventasNetas'))} icono="↗" detalle="Todos los bares, sin devoluciones" />
        <Kpi etiqueta="Comandas cobradas" valor={suma('comandasCobradas')} icono="✓" detalle={`${suma('unidadesVendidas')} tragos vendidos`} />
        <Kpi etiqueta="Insumos críticos" valor={criticos.length} icono="◈"
             tono={criticos.length ? 'warn' : undefined} detalle="En o bajo el punto de reposición" />
        <Kpi etiqueta="Merma auditada · 7 días" valor={pesos(suma('mermas'))} icono="⊙" detalle="Diferencias de los recuentos" />
      </div>

      <div className="cols-2 mt16">
        <Tarjeta titulo="Tus establecimientos" subtitulo="Últimos 7 días de cada bar">
          {filas.length ? (
            <div className="cols-half" style={{ gap: 12 }}>
              {filas.map(({ bar, indicadores, criticos: deBar, insumos }) => (
                <div className="bar-resumen" key={bar.id}>
                  <div className="flex between">
                    <h3>{bar.nombre}</h3>
                    {bar.id === establecimientoId ? <Pill tono="success">Activo</Pill>
                      : <button className="btn btn-small" type="button" onClick={() => cambiarEstablecimiento(bar.id)}>Ver</button>}
                  </div>
                  <div className="record-sub">{bar.direccion ? `${bar.direccion} · ` : ''}{bar.zona}</div>
                  <div className="record-sub mb8">Mediodía {horario('MEDIODIA', bar.horario)} · Noche {horario('NOCHE', bar.horario)}</div>
                  <div className="record"><span className="small">Ventas netas</span><b>{pesos(indicadores.ventasNetas)}</b></div>
                  <div className="record"><span className="small">Contribución</span><b className="text-green">{pesos(indicadores.contribucion)}</b></div>
                  <div className="record"><span className="small">Merma</span><b className={Number(indicadores.mermas) ? 'text-red' : ''}>{pesos(indicadores.mermas)}</b></div>
                  <div className="record"><span className="small">Insumos críticos</span><b>{deBar.length} / {insumos}</b></div>
                </div>
              ))}
            </div>
          ) : <Vacio>{resumen.cargando ? 'Cargando…' : 'No hay establecimientos.'}</Vacio>}
        </Tarjeta>
        <Tarjeta titulo="Para reponer" subtitulo="Insumos en su punto de reposición o por debajo, en todos los bares"
                 extra={<Pill tono={criticos.length ? 'warn' : 'success'}>{criticos.length}</Pill>}>
          {criticos.length ? criticos.map(({ bar, insumo }) => (
            <div className="alert" key={insumo.id}>
              <h4>{insumo.nombre} · {bar.nombre}</h4>
              <p>Quedan {enEnvases(insumo.stockActual, insumo.contenidoEnvase, insumo.unidadMedida)}.</p>
            </div>
          )) : <div className="note success">Todos los insumos de tus bares están por encima de su punto de reposición.</div>}
          <button className="btn mt12" type="button" onClick={() => ir('reportes')}>Ver tendencias y mermas →</button>
        </Tarjeta>
      </div>
    </>
  );
}
