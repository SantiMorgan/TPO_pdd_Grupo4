import { useState } from 'react';
import { api } from '../../api/client.js';
import { pesos } from '../../components/formato.js';
import { Cargando, Encabezado, Kpi, Tarjeta } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';
import { SeccionReporte } from './secciones.jsx';

const OPCIONES = [
  ['GRAFICO', 'Ranking de tragos', 'ReporteConGrafico'],
  ['TENDENCIAS', 'Tendencia de ventas', 'ReporteConTendencias'],
  ['MERMAS', 'Mermas por insumo', 'ReporteConMermas'],
  ['MARGEN', 'Margen por trago', 'ReporteConMargen'],
];

export default function ReportesView() {
  const { establecimientoId } = useSesion();
  const [secciones, setSecciones] = useState(['GRAFICO', 'TENDENCIAS']);
  // Sin refresco automático: cada generación con secciones queda en la traza del Decorator.
  const reporte = useDatos(() => api.reporte(establecimientoId, secciones), [establecimientoId, secciones.join(',')]);

  const alternar = (tipo) => setSecciones((actuales) => (actuales.includes(tipo)
    ? actuales.filter((t) => t !== tipo) : [...actuales, tipo]));

  return (
    <>
      <Encabezado titulo="Reportes y rentabilidad"
                  descripcion="El reporte base calcula ventas, costos, compras y mermas. Cada sección que elijas lo envuelve con un decorador, en el orden en que la marcás." />
      <Tarjeta titulo="Armá tu reporte" subtitulo="Patrón Decorator: el backend compone la cadena en tiempo de ejecución"
               extra={<button className="btn btn-small" type="button" onClick={reporte.recargar}>↻ Actualizar</button>}>
        <div className="casillas">
          {OPCIONES.map(([tipo, nombre, clase]) => (
            <label key={tipo} className={`casilla${secciones.includes(tipo) ? ' activa' : ''}`}>
              <input type="checkbox" checked={secciones.includes(tipo)} onChange={() => alternar(tipo)} />
              {nombre} <span className="tiny muted mono">{clase}</span>
            </label>
          ))}
        </div>
        {reporte.datos && (
          <div className="cadena mt16" aria-label="Cadena de decoradores">
            {reporte.datos.composicion.map((paso, i) => (
              <span key={paso} className="flex gap6">
                {i > 0 && <span className="muted">→</span>}
                <span className={`eslabon${i === 0 ? ' base' : ''}`}>{paso}</span>
              </span>
            ))}
          </div>
        )}
      </Tarjeta>
      <Cargando estado={reporte}>
        {(datos) => {
          const i = datos.indicadores;
          return (
            <>
              <div className="cols-4 mt16">
                <Kpi etiqueta="Ventas netas" valor={pesos(i.ventasNetas)} detalle={`${i.comandasCobradas} comandas · ticket ${pesos(i.ticketPromedio)}`} />
                <Kpi etiqueta="Contribución por tragos" valor={pesos(i.contribucion)} tono={Number(i.contribucion) < 0 ? 'warn' : 'accent'}
                     detalle={i.margenContribucion !== null ? `Margen ${i.margenContribucion} % sobre ventas` : 'Ventas menos insumos consumidos'} />
                <Kpi etiqueta="Compras registradas" valor={pesos(i.compras)} detalle="Entradas de stock (no se restan otra vez)" />
                <Kpi etiqueta="Pérdidas por merma" valor={pesos(i.mermas)} tono="warn" detalle={`${i.auditorias} cierre(s) auditados`} />
              </div>
              <div className="cols-half mt16">
                {datos.secciones.map((s) => <SeccionReporte key={s.tipo} seccion={s} />)}
              </div>
              <Tarjeta titulo="Transparencia de cálculo" subtitulo="Cifras orientativas" className="mt16">
                <div className="flow">
                  <div className="secondary-stat"><div className="kpi-label">Costo de insumos consumidos</div><strong>{pesos(i.costoInsumos)}</strong><div className="tiny muted">Incluye comandas despachadas que luego se devolvieron.</div></div>
                  <div className="secondary-stat"><div className="kpi-label">Ingresos revertidos por devoluciones</div><strong>{pesos(i.devoluciones)}</strong><div className="tiny muted">{i.comandasDevueltas} devolución(es); los insumos no se reintegran.</div></div>
                  <div className="secondary-stat"><div className="kpi-label">Unidades vendidas</div><strong>{i.unidadesVendidas}</strong><div className="tiny muted">Tragos de comandas cobradas.</div></div>
                </div>
                <div className="note mt16">No incluye sueldos, alquiler, impuestos ni otros costos operativos: la contribución no es la utilidad neta del negocio.</div>
              </Tarjeta>
            </>
          );
        }}
      </Cargando>
    </>
  );
}
