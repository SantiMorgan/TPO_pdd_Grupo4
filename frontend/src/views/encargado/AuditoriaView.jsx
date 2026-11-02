import { api } from '../../api/client.js';
import { pesos } from '../../components/formato.js';
import { HistorialAuditorias, RecuentoFisico } from '../../components/inventario.jsx';
import { Cargando, Encabezado, Kpi, Pill, Tarjeta } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

const PASOS = [
  ['1', 'Stock teórico', 'Lo que debería haber: stock inicial + compras − tragos despachados ± recuentos anteriores. Se muestra en ml y en botellas.'],
  ['2', 'Conteo físico', 'Contá las botellas cerradas y medí cuánto queda en la abierta. El sistema lo pasa a mililitros.'],
  ['3', 'Diferencia y comentario', 'Diferencia = físico − teórico. Si falta algo, se valoriza como merma al costo actual. Anotá el motivo: una botella rota, tragos de cortesía (con el nombre del bartender), un derrame…'],
];

export default function AuditoriaView() {
  const { establecimientoId, sesion } = useSesion();
  const insumos = useDatos(() => api.insumos.listar(establecimientoId), [establecimientoId]);
  const auditorias = useDatos(() => api.auditorias.listar(establecimientoId), [establecimientoId], { intervalo: 10000 });
  const lista = auditorias.datos || [];
  const mermaTotal = lista.reduce((total, a) => total + Number(a.mermaTotal), 0);

  return (
    <>
      <Encabezado titulo="Auditoría de inventario"
                  descripcion="Compará lo que debería haber contra lo que realmente hay, ajustá el stock y dejá anotado por qué hubo diferencias." />
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
          {PASOS.map(([n, t, d]) => (
            <div className="step" key={n}><div className="step-n">{n}</div><div><h4>{t}</h4><p>{d}</p></div></div>
          ))}
          <div className="note info mt16">Al registrar el cierre, el stock queda igual a lo contado. Si algún insumo queda en su punto de reposición o por debajo, se envía la alerta.</div>
        </Tarjeta>
      </div>
      <Tarjeta titulo="Historial de cierres" subtitulo="Tocá un cierre para ver el detalle por insumo y los comentarios" className="mt16"
               extra={<Pill>{lista.length} auditorías</Pill>}>
        <HistorialAuditorias auditorias={lista} detallado />
      </Tarjeta>
    </>
  );
}
