import { api } from '../../api/client.js';
import { pesos } from '../../components/formato.js';
import { TrazaPatrones } from '../../components/Patrones.jsx';
import { Kpi, Pill, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

const RECORRIDO = [
  ['01', 'Cliente pide desde la carta', 'El precio sale de la estrategia vigente (Strategy) y se fija al registrar.'],
  ['02', 'Bartender prepara y despacha', 'State habilita cada paso; la Facade descuenta todos los insumos o ninguno.'],
  ['03', 'Alerta de reposición', 'Inventario avisa a sus observadores; AlertaStock usa la NotificacionFactory.'],
  ['04', 'Encargado compra y audita', 'Compras por envase y recuento físico con merma valorizada.'],
  ['05', 'Dueño analiza', 'Reportes compuestos con decoradores y política de precios por bar.'],
];

export default function ResumenView({ ir }) {
  const { establecimientos, establecimientoId, cambiarEstablecimiento } = useSesion();
  // Un reporte base y el stock de cada bar: el dueño compara todos sus establecimientos.
  const resumen = useDatos(() => Promise.all(establecimientos.map(async (bar) => {
    const [reporte, insumos] = await Promise.all([api.reporte(bar.id), api.insumos.listar(bar.id)]);
    return { bar, indicadores: reporte.indicadores, criticos: insumos.filter((i) => i.necesitaReposicion).length, insumos: insumos.length };
  })), [establecimientos.map((b) => b.id).join(',')], { intervalo: 15000 });
  const filas = resumen.datos || [];
  const suma = (campo) => filas.reduce((total, f) => total + Number(f.indicadores[campo]), 0);

  return (
    <>
      <section className="hero">
        <div>
          <div className="eyebrow">Panel del dueño · {establecimientos.length} establecimiento(s)</div>
          <h1>Cada mililitro cuenta.<br /> Cada decisión también.</h1>
          <p>Del trago despachado al inventario real: recetas, trazabilidad, precios dinámicos y detección de mermas en todos tus bares.</p>
          <div className="flex gap8 wrap">
            <button className="btn btn-primary" type="button" onClick={() => ir('reportes')}>Ver reportes →</button>
            <button className="btn btn-dark" type="button" onClick={() => ir('precios')}>Configurar precios</button>
          </div>
        </div>
        <div className="hero-art">🍸</div>
      </section>

      <div className="cols-4">
        <Kpi etiqueta="Ventas netas (todos los bares)" valor={pesos(suma('ventasNetas'))} icono="↗" detalle="Despachadas, sin devoluciones" />
        <Kpi etiqueta="Comandas cobradas" valor={suma('comandasCobradas')} icono="✓" detalle={`${suma('unidadesVendidas')} tragos vendidos`} />
        <Kpi etiqueta="Insumos críticos" valor={filas.reduce((t, f) => t + f.criticos, 0)} icono="◈"
             tono={filas.some((f) => f.criticos) ? 'warn' : undefined} detalle="En o bajo el punto de reposición" />
        <Kpi etiqueta="Merma auditada" valor={pesos(suma('mermas'))} icono="⊙" detalle="Diferencias físicas negativas" />
      </div>

      <div className="cols-2 mt16">
        <Tarjeta titulo="Tus establecimientos" subtitulo="Cada bar tiene su menú, inventario y comandas independientes">
          {filas.length ? (
            <div className="cols-half" style={{ gap: 12 }}>
              {filas.map(({ bar, indicadores, criticos, insumos }) => (
                <div className="bar-resumen" key={bar.id}>
                  <div className="flex between">
                    <h3>{bar.nombre}</h3>
                    {bar.id === establecimientoId ? <Pill tono="success">Activo</Pill>
                      : <button className="btn btn-small" type="button" onClick={() => cambiarEstablecimiento(bar.id)}>Ver</button>}
                  </div>
                  <div className="record-sub mb8">{bar.zona} · {bar.descripcion}</div>
                  <div className="record"><span className="small">Ventas netas</span><b>{pesos(indicadores.ventasNetas)}</b></div>
                  <div className="record"><span className="small">Contribución</span><b className="text-green">{pesos(indicadores.contribucion)}</b></div>
                  <div className="record"><span className="small">Merma</span><b className={Number(indicadores.mermas) ? 'text-red' : ''}>{pesos(indicadores.mermas)}</b></div>
                  <div className="record"><span className="small">Insumos críticos</span><b>{criticos} / {insumos}</b></div>
                  <div className="record"><span className="small">Precios</span><Pill>{bar.politicaPrecio.modo}</Pill></div>
                </div>
              ))}
            </div>
          ) : <Vacio>{resumen.cargando ? 'Cargando…' : 'No hay establecimientos.'}</Vacio>}
        </Tarjeta>
        <div>
          <Tarjeta titulo="Recorrido sugerido para la exposición" subtitulo="Cambiá de rol desde la barra lateral" extra={<Pill tono="info">5 minutos</Pill>}>
            {RECORRIDO.map(([n, t, d]) => (
              <div className="step" key={n}><div className="step-n">{n}</div><div><h4>{t}</h4><p>{d}</p></div></div>
            ))}
          </Tarjeta>
          <Tarjeta titulo="Actividad de patrones" subtitulo="Establecimiento activo">
            <TrazaPatrones limite={8} />
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
