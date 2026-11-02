import { cantidad, diaMes, diaSemana, nombreEnvase, numero, periodoTexto, pesos, variacionTexto } from '../../components/formato.js';
import { Tarjeta, Vacio } from '../../components/ui.jsx';

// Cada sección que pide el dueño llega del servidor con sus propios datos; acá solo se dibujan.

export function Ranking({ datos }) {
  if (!datos.length) return <Vacio>No hubo ventas en este período.</Vacio>;
  const maximo = datos[0].unidades;
  return datos.map((b) => (
    <div className="bar-chart" key={b.trago}>
      <div className="bar-label">{b.trago}</div>
      <div className="bar-track"><div className="bar-fill" style={{ width: `${Math.max(3, (b.unidades / maximo) * 100)}%` }} /></div>
      <div className="bar-total">{b.unidades}</div>
    </div>
  ));
}

/** Hasta un mes se ve jornada por jornada; períodos más largos se agrupan por semana. */
function serie(porJornada) {
  if (porJornada.length <= 31) {
    return porJornada.map((d) => ({ etiqueta: diaSemana(d.jornada), ventas: Number(d.ventas) }));
  }
  const semanas = [];
  porJornada.forEach((d, i) => {
    if (i % 7 === 0) semanas.push({ etiqueta: `Semana del ${diaMes(d.jornada)}`, ventas: 0 });
    semanas[semanas.length - 1].ventas += Number(d.ventas);
  });
  return semanas;
}

const conVariacion = (lista) => lista.filter((x) => x.variacionPorcentaje !== null);
const porcentaje = (valor) => numero(Math.abs(Number(valor)));

/** Sugerencias a partir de lo que cambió: qué bar o qué trago mirar y qué decisión tomar. */
function consejos(datos) {
  const lista = [];
  const bares = conVariacion(datos.porEstablecimiento);
  if (datos.porEstablecimiento.length > 1 && bares.length) {
    const ordenados = [...bares].sort((a, b) => Number(a.variacionPorcentaje) - Number(b.variacionPorcentaje));
    const peor = ordenados[0];
    const mejor = ordenados[ordenados.length - 1];
    if (Number(peor.variacionPorcentaje) < -5) {
      lista.push(`${peor.nombre} vendió ${porcentaje(peor.variacionPorcentaje)} % menos que en el período anterior: revisá su carta, sus precios y qué pasó en cada turno antes de decidir compras o personal.`);
    }
    if (mejor !== peor && Number(mejor.variacionPorcentaje) > 5) {
      lista.push(`${mejor.nombre} vendió ${porcentaje(mejor.variacionPorcentaje)} % más: reforzá sus compras y revisá los puntos de reposición para que no le falte stock.`);
    }
  }
  const tragos = [...datos.porTrago].sort((a, b) => (b.unidadesActual - b.unidadesAnterior) - (a.unidadesActual - a.unidadesAnterior));
  const sube = tragos.find((t) => t.unidadesActual > t.unidadesAnterior);
  const baja = [...tragos].reverse().find((t) => t.unidadesActual < t.unidadesAnterior);
  if (sube) {
    lista.push(`${sube.trago} pasó de ${sube.unidadesAnterior} a ${sube.unidadesActual} unidades: asegurate de tener stock de sus insumos.`);
  }
  if (baja) {
    lista.push(`${baja.trago} bajó de ${baja.unidadesAnterior} a ${baja.unidadesActual} unidades: probá sumarlo al Happy Hour, revisá su precio o evaluá sacarlo del menú.`);
  }
  if (!lista.length) lista.push('Las ventas se mantienen parecidas a las del período anterior.');
  lista.push('Usá el filtro de turno para ver si el cambio viene del mediodía o de la noche y ajustar horarios y personal.');
  return lista;
}

function Tendencia({ datos }) {
  const dias = serie(datos.porJornada);
  const maximo = Math.max(1, ...dias.map((d) => d.ventas));
  const variacion = datos.variacionPorcentaje;
  const tono = variacion === null ? '' : Number(variacion) >= 0 ? 'text-green' : 'text-red';
  return (
    <>
      <div className="cols-3" style={{ gap: 10 }}>
        <div className="secondary-stat">
          <div className="kpi-label">Período elegido · {periodoTexto(datos.periodo)}</div>
          <strong>{pesos(datos.ventasActual)}</strong>
          <div className="tiny muted">{datos.comandasActual} comandas cobradas</div>
        </div>
        <div className="secondary-stat">
          <div className="kpi-label">Período anterior · {periodoTexto(datos.periodoAnterior)}</div>
          <strong>{pesos(datos.ventasAnterior)}</strong>
          <div className="tiny muted">{datos.comandasAnterior} comandas cobradas</div>
        </div>
        <div className="secondary-stat">
          <div className="kpi-label">Variación</div>
          <strong className={tono}>{variacionTexto(variacion)}</strong>
          <div className="tiny muted">Misma cantidad de jornadas y mismo turno</div>
        </div>
      </div>

      <div className="small strong mt16 mb8">Ventas por jornada</div>
      {dias.map((d) => (
        <div className="bar-chart" key={d.etiqueta}>
          <div className="bar-label">{d.etiqueta}</div>
          <div className="bar-track"><div className="bar-fill" style={{ width: `${d.ventas > 0 ? Math.max(2, (d.ventas / maximo) * 100) : 0}%` }} /></div>
          <div className="bar-total bar-total-ancho">{pesos(d.ventas)}</div>
        </div>
      ))}

      {datos.porEstablecimiento.length > 1 && (
        <>
          <div className="small strong mt16 mb8">Por establecimiento</div>
          <p className="tiny muted mb8">
            Con «Todos los establecimientos» las ventas de arriba son la suma de todos los bares. Acá ves cuánto aportó cada uno y si subió o bajó: así sabés de dónde viene el cambio.
          </p>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Bar</th><th className="right">Período elegido</th><th className="right">Período anterior</th><th className="right">Variación</th></tr></thead>
              <tbody>
                {datos.porEstablecimiento.map((b) => (
                  <tr key={b.id}>
                    <td>{b.nombre}</td>
                    <td className="right">{pesos(b.ventasActual)}</td>
                    <td className="right muted">{pesos(b.ventasAnterior)}</td>
                    <td className={`right ${b.variacionPorcentaje === null ? '' : Number(b.variacionPorcentaje) >= 0 ? 'text-green' : 'text-red'}`}>
                      {variacionTexto(b.variacionPorcentaje)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      {datos.porTrago.length > 0 && (
        <>
          <div className="small strong mt16 mb8">Tragos que más cambiaron</div>
          <div className="table-wrap">
            <table>
              <thead><tr><th>Trago</th><th className="right">Unidades ahora</th><th className="right">Antes</th><th className="right">Variación</th></tr></thead>
              <tbody>
                {datos.porTrago.map((t) => (
                  <tr key={t.trago}>
                    <td>{t.trago}</td>
                    <td className="right">{t.unidadesActual}</td>
                    <td className="right muted">{t.unidadesAnterior}</td>
                    <td className={`right ${t.unidadesActual >= t.unidadesAnterior ? 'text-green' : 'text-red'}`}>{variacionTexto(t.variacionPorcentaje)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}

      <div className="note info mt16">
        <b>¿Qué podés decidir con esto?</b>
        <ul className="consejos">{consejos(datos).map((texto) => <li key={texto}>{texto}</li>)}</ul>
      </div>
    </>
  );
}

function Mermas({ datos }) {
  if (!datos.length) return <Vacio>No hubo mermas en los recuentos de este período.</Vacio>;
  const variosBares = new Set(datos.map((m) => m.establecimiento)).size > 1;
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {variosBares && <th>Bar</th>}<th>Insumo</th><th className="right">Perdido</th><th className="right">Unidades perdidas</th>
            <th className="right">Costo</th><th>Motivos anotados</th>
          </tr>
        </thead>
        <tbody>{datos.map((m) => (
          <tr key={`${m.establecimiento}-${m.insumo}`}>
            {variosBares && <td>{m.establecimiento}</td>}
            <td>{m.insumo}</td>
            <td className="right">{cantidad(m.cantidadPerdida, m.unidad)}</td>
            <td className="right">{numero(m.envasesPerdidos)} {nombreEnvase(m.unidad, Number(m.envasesPerdidos) !== 1)}</td>
            <td className="right text-red">{pesos(m.costo)}</td>
            <td className="texto-largo">{m.comentarios.length ? m.comentarios.join(' · ') : <span className="muted">Sin comentarios</span>}</td>
          </tr>
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
            <td className="right">{pesos(m.costo)}</td><td className={`right ${Number(m.contribucion) < 0 ? 'text-red' : 'text-green'}`}>{pesos(m.contribucion)}</td>
            <td className="right">{m.margenPorcentaje === null ? '—' : `${numero(m.margenPorcentaje)} %`}</td>
          </tr>
        ))}</tbody>
      </table>
    </div>
  );
}

const COMPONENTES = { GRAFICO: Ranking, TENDENCIAS: Tendencia, MERMAS: Mermas, MARGEN: Margen };
const DESCRIPCION = {
  GRAFICO: 'Unidades vendidas de cada trago, sin contar las devueltas',
  TENDENCIAS: 'El período elegido contra el anterior de la misma duración',
  MERMAS: 'Lo que faltó en los recuentos, en mililitros y en botellas',
  MARGEN: 'Lo cobrado menos el costo de los insumos de cada trago',
};

export function SeccionReporte({ seccion }) {
  const Componente = COMPONENTES[seccion.tipo];
  return (
    <Tarjeta titulo={seccion.titulo} subtitulo={DESCRIPCION[seccion.tipo]}
             className={seccion.tipo === 'TENDENCIAS' || seccion.tipo === 'MERMAS' ? 'ancho-completo' : ''}>
      <Componente datos={seccion.datos} />
    </Tarjeta>
  );
}
