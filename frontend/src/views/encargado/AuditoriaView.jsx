import { api } from '../../api/client.js';
import { pesos } from '../../components/formato.js';
import { HistorialAuditorias, RecuentoFisico } from '../../components/inventario.jsx';
import { Cargando, Encabezado, Kpi, Pill, Tarjeta } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

export default function AuditoriaView() {
  const { establecimientoId, sesion } = useSesion();
  const insumos = useDatos(() => api.insumos.listar(establecimientoId), [establecimientoId]);
  const auditorias = useDatos(() => api.auditorias.listar(establecimientoId), [establecimientoId], { intervalo: 10000 });
  const lista = auditorias.datos || [];
  const mermaTotal = lista.reduce((total, a) => total + Number(a.mermaTotal), 0);

  return (
    <>
      <Encabezado titulo="Auditoría de inventario"
                  descripcion="Compará las existencias calculadas contra el recuento físico, ajustá el stock y valorizá la merma con trazabilidad." />
      <div className="cols-3 mb14">
        <Kpi etiqueta="Cierres registrados" valor={lista.length} detalle="Incluye los cargados por el bartender" />
        <Kpi etiqueta="Merma acumulada" valor={pesos(mermaTotal)} tono={mermaTotal ? 'warn' : undefined} detalle="Diferencias negativas valorizadas" />
        <Kpi etiqueta="Último cierre" valor={lista[0] ? `#${lista[0].numero}` : '—'} detalle={lista[0]?.responsable || 'Sin cierres'} />
      </div>
      <div className="cols-2" style={{ gridTemplateColumns: '1.4fr .8fr' }}>
        <Cargando estado={insumos}>
          {(datos) => (
            <RecuentoFisico insumos={datos} responsable={`Encargado${sesion.nombre ? ` · ${sesion.nombre}` : ''}`}
                            onRegistrado={() => { insumos.recargar(); auditorias.recargar(); }} />
          )}
        </Cargando>
        <Tarjeta titulo="Cómo se calcula" subtitulo="Regla de auditoría">
          {[['1', 'Stock teórico', 'Stock inicial + compras − consumos por despacho ± ajustes anteriores.'],
            ['2', 'Conteo físico', 'Lo que realmente se midió al cierre.'],
            ['3', 'Diferencia y ajuste', 'Diferencia = físico − teórico. Si es negativa se valoriza como merma al costo actual.']].map(([n, t, d]) => (
            <div className="step" key={n}><div className="step-n">{n}</div><div><h4>{t}</h4><p>{d}</p></div></div>
          ))}
          <div className="note info mt16">El recuento lleva el stock al valor físico y avisa a los observadores, así que también puede disparar una alerta de reposición.</div>
        </Tarjeta>
      </div>
      <Tarjeta titulo="Historial de cierres" subtitulo="Tocá un cierre para ver el detalle por insumo" className="mt16"
               extra={<Pill>{lista.length} auditorías</Pill>}>
        <HistorialAuditorias auditorias={lista} detallado />
      </Tarjeta>
    </>
  );
}
