import { cantidad, pesos } from '../../components/formato.js';
import { Tarjeta, Vacio } from '../../components/ui.jsx';

// Cada decorador del backend agrega una sección con su propio tipo de datos; acá solo se dibujan.

function Ranking({ datos }) {
  if (!datos.length) return <Vacio>Despachá alguna comanda para ver el ranking.</Vacio>;
  const maximo = datos[0].unidades;
  return datos.map((b) => (
    <div className="bar-chart" key={b.trago}>
      <div className="bar-label">{b.trago}</div>
      <div className="bar-track"><div className="bar-fill" style={{ width: `${Math.max(3, (b.unidades / maximo) * 100)}%` }} /></div>
      <div className="bar-total">{b.unidades}</div>
    </div>
  ));
}

function Tendencia({ datos }) {
  if (!datos.comparable) return <Vacio>Se necesitan al menos dos despachos para comparar.</Vacio>;
  return (
    <>
      <div className="cols-half" style={{ gap: 10 }}>
        <div className="secondary-stat"><div className="kpi-label">Primera mitad ({datos.despachosPrimeraMitad} despachos)</div><strong>{pesos(datos.primeraMitad)}</strong></div>
        <div className="secondary-stat"><div className="kpi-label">Segunda mitad ({datos.despachosSegundaMitad} despachos)</div><strong>{pesos(datos.segundaMitad)}</strong></div>
      </div>
      {datos.variacionPorcentaje !== null && (
        <div className={`note ${datos.variacionPorcentaje >= 0 ? 'success' : 'danger'} mt12`}>
          Variación: {datos.variacionPorcentaje >= 0 ? '+' : ''}{datos.variacionPorcentaje} %. Comparación por orden de despacho; no es una predicción.
        </div>
      )}
    </>
  );
}

function Mermas({ datos }) {
  if (!datos.length) return <Vacio>No hay mermas auditadas.</Vacio>;
  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Insumo</th><th className="right">Perdido</th><th className="right">Costo</th></tr></thead>
        <tbody>{datos.map((m) => (
          <tr key={m.insumo}><td>{m.insumo}</td><td className="right">{cantidad(m.cantidadPerdida, m.unidad)}</td><td className="right text-red">{pesos(m.costo)}</td></tr>
        ))}</tbody>
      </table>
    </div>
  );
}

function Margen({ datos }) {
  if (!datos.length) return <Vacio>Sin ventas para calcular márgenes.</Vacio>;
  return (
    <div className="table-wrap">
      <table>
        <thead><tr><th>Trago</th><th className="right">Unid.</th><th className="right">Ingresos</th><th className="right">Costo</th><th className="right">Contribución</th><th className="right">Margen</th></tr></thead>
        <tbody>{datos.map((m) => (
          <tr key={m.trago}>
            <td>{m.trago}</td><td className="right">{m.unidades}</td><td className="right">{pesos(m.ingresos)}</td>
            <td className="right">{pesos(m.costo)}</td><td className="right text-green">{pesos(m.contribucion)}</td>
            <td className="right">{m.margenPorcentaje ?? '—'} %</td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}

const COMPONENTES = { GRAFICO: Ranking, TENDENCIAS: Tendencia, MERMAS: Mermas, MARGEN: Margen };
const DECORADOR = { GRAFICO: 'ReporteConGrafico', TENDENCIAS: 'ReporteConTendencias', MERMAS: 'ReporteConMermas', MARGEN: 'ReporteConMargen' };

export function SeccionReporte({ seccion }) {
  const Componente = COMPONENTES[seccion.tipo];
  return (
    <Tarjeta titulo={seccion.titulo} subtitulo={`Agregada por ${DECORADOR[seccion.tipo]}`}>
      <Componente datos={seccion.datos} />
    </Tarjeta>
  );
}
