import { Fragment, useMemo, useState } from 'react';
import { api } from '../api/client.js';
import { useSesion } from '../session/Sesion.jsx';
import { useAccion } from './Avisos.jsx';
import {
  cantidad, conSigno, enEnvases, envasesCerrados, envasesDecimal, fechaHora, nombreEnvase, pesos, pesosConDecimales, unidad,
} from './formato.js';
import { EstadoStock, Kpi, Nivel, Pill, Tarjeta, Vacio } from './ui.jsx';

export function KpisStock({ insumos }) {
  const criticos = insumos.filter((i) => i.necesitaReposicion);
  const valor = insumos.reduce((total, i) => total + Number(i.valorStock), 0);
  return (
    <div className="cols-3 mb14">
      <Kpi etiqueta="Insumos activos" valor={insumos.length} detalle="Catálogo del establecimiento" />
      <Kpi etiqueta="Stock crítico" valor={criticos.length} tono={criticos.length ? 'warn' : undefined} detalle="Igual o menor al punto de reposición" />
      <Kpi etiqueta="Valorización del stock" valor={pesos(valor)} detalle="Al costo actual" />
    </div>
  );
}

/** Stock en mililitros y en botellas. Sin {@code acciones} es de solo lectura (bartender). */
export function TablaStock({ insumos, acciones }) {
  if (!insumos.length) return <Vacio simbolo="▤">No hay insumos cargados.</Vacio>;
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Insumo</th><th className="right">Stock</th><th>En botellas</th><th>Nivel</th><th className="right">Reposición</th>
            <th className="right">Costo unitario</th><th>Estado</th>{acciones && <th>Acciones</th>}
          </tr>
        </thead>
        <tbody>
          {insumos.map((i) => (
            <tr key={i.id}>
              <td><b>{i.nombre}</b><div className="record-sub">{i.codigo} · {nombreEnvase(i.unidadMedida, false)} de {cantidad(i.contenidoEnvase, i.unidadMedida)} · {pesos(i.costoEnvase)}</div></td>
              <td className={`right ${i.necesitaReposicion ? 'text-red strong' : ''}`}>{cantidad(i.stockActual, i.unidadMedida)}</td>
              <td>{enEnvases(i.stockActual, i.contenidoEnvase, i.unidadMedida)}</td>
              <td><Nivel insumo={i} /></td>
              <td className="right">{cantidad(i.puntoReorden, i.unidadMedida)}</td>
              <td className="right">{pesosConDecimales(i.costoUnitario)}/{unidad(i.unidadMedida)}</td>
              <td><EstadoStock insumo={i} /></td>
              {acciones && <td><div className="flex gap6">{acciones(i)}</div></td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function AlertasActivas({ insumos }) {
  const criticos = insumos.filter((i) => i.necesitaReposicion);
  if (!criticos.length) return <div className="note success">Todos los insumos están por encima de su punto de reposición.</div>;
  return criticos.map((i) => (
    <div className="alert" key={i.id}>
      <h4>{i.nombre} · reposición necesaria</h4>
      <p>Quedan {cantidad(i.stockActual, i.unidadMedida)} ({enEnvases(i.stockActual, i.contenidoEnvase, i.unidadMedida)}). Punto de reposición: {cantidad(i.puntoReorden, i.unidadMedida)}.</p>
    </div>
  ));
}

export function Movimientos({ movimientos }) {
  if (!movimientos.length) return <Vacio>Todavía no hay movimientos.</Vacio>;
  const etiqueta = { COMPRA: 'Compra', CONSUMO: 'Consumo', AJUSTE: 'Ajuste por recuento' };
  return movimientos.map((m) => (
    <div className="record" key={m.id}>
      <div>
        <div className="record-label">{etiqueta[m.tipo]} · {m.insumo}</div>
        <div className="record-sub">{m.detalle} · {fechaHora(m.fecha)}{Number(m.monto) ? ` · ${pesos(m.monto)}` : ''}</div>
      </div>
      <b className={`small ${Number(m.cantidad) < 0 ? 'text-red' : 'text-green'}`}>{conSigno(m.cantidad)} {unidad(m.unidadMedida)}</b>
    </div>
  ));
}

const VACIO = { envases: '', resto: '', comentario: '' };
const esEnteroNoNegativo = (texto) => texto === '' || (Number.isInteger(Number(texto)) && Number(texto) >= 0);

/**
 * Recuento físico de cierre de turno: botellas cerradas más lo que queda en la abierta, con un
 * comentario para justificar la diferencia. Muestra una merma preliminar mientras se carga; la
 * diferencia y la merma definitivas las calcula y guarda el servidor.
 */
export function RecuentoFisico({ insumos, responsable, onRegistrado }) {
  const { establecimientoId } = useSesion();
  const [conteos, setConteos] = useState({});
  const [ejecutar, ocupado] = useAccion();

  const cambiar = (id, campo, valor) => setConteos((c) => ({ ...c, [id]: { ...VACIO, ...c[id], [campo]: valor } }));

  const filas = useMemo(() => insumos.map((i) => {
    const conteo = conteos[i.id] || VACIO;
    const cargado = conteo.envases !== '' || conteo.resto !== '';
    const envases = Number(conteo.envases || 0);
    const resto = Number(conteo.resto || 0);
    const valido = cargado && esEnteroNoNegativo(conteo.envases) && esEnteroNoNegativo(conteo.resto)
      && resto <= i.contenidoEnvase;
    const fisico = valido ? envases * i.contenidoEnvase + resto : null;
    const diferencia = valido ? fisico - Number(i.stockActual) : null;
    const merma = diferencia !== null && diferencia < 0 ? -diferencia * Number(i.costoUnitario) : 0;
    return { insumo: i, conteo, cargado, valido, envases, resto, fisico, diferencia, merma };
  }), [insumos, conteos]);
  const contados = filas.filter((f) => f.valido);
  const conError = filas.some((f) => f.cargado && !f.valido);
  const mermaPreliminar = contados.reduce((total, f) => total + f.merma, 0);

  const simular = () => {
    const candidato = insumos.find((i) => Number(i.stockActual) >= 20) || insumos[0];
    if (!candidato) return;
    const fisico = Math.max(0, Number(candidato.stockActual) - 20);
    setConteos((c) => ({
      ...c,
      [candidato.id]: {
        envases: String(Math.floor(fisico / candidato.contenidoEnvase)),
        resto: String(fisico % candidato.contenidoEnvase),
        comentario: 'Derrame al servir',
      },
    }));
  };

  const registrar = async () => {
    const auditoria = await ejecutar(
      () => api.auditorias.registrar(establecimientoId, responsable, contados.map((f) => ({
        insumoId: f.insumo.id, envasesCompletos: f.envases, restante: f.resto, comentario: f.conteo.comentario.trim() || null,
      }))),
      (a) => `Cierre #${a.numero} registrado. Merma detectada: ${pesos(a.mermaTotal)}`,
    );
    if (auditoria) {
      setConteos({});
      onRegistrado?.(auditoria);
    }
  };

  return (
    <Tarjeta titulo="Recuento físico"
             subtitulo="Contá las botellas cerradas y cuánto queda en la abierta. Completá solo los insumos que contaste."
             extra={<button className="btn btn-small" type="button" onClick={simular}>◈ Simular diferencia física</button>}>
      <div className="table-wrap">
        <table className="recuento">
          <thead>
            <tr>
              <th>Insumo</th><th className="right">Teórico</th><th>Cerradas</th><th>Queda en la abierta</th>
              <th className="right">Físico</th><th className="right">Diferencia</th><th className="right">Costo merma</th>
            </tr>
          </thead>
          <tbody>
            {filas.map(({ insumo, conteo, cargado, valido, fisico, diferencia, merma }) => (
              <Fragment key={insumo.id}>
                <tr className={cargado ? 'con-comentario' : ''}>
                  <td><b>{insumo.nombre}</b><div className="record-sub">{nombreEnvase(insumo.unidadMedida, false)} de {cantidad(insumo.contenidoEnvase, insumo.unidadMedida)}</div></td>
                  <td className="right">
                    {cantidad(insumo.stockActual, insumo.unidadMedida)}
                    <div className="record-sub">{enEnvases(insumo.stockActual, insumo.contenidoEnvase, insumo.unidadMedida)}</div>
                  </td>
                  <td>
                    <input className="input-tiny" type="number" min="0" step="1" inputMode="numeric"
                           aria-label={`${envasesCerrados(insumo.unidadMedida)} de ${insumo.nombre}`}
                           placeholder={String(Math.floor(Number(insumo.stockActual) / insumo.contenidoEnvase))} value={conteo.envases}
                           onChange={(e) => cambiar(insumo.id, 'envases', e.target.value)} />
                  </td>
                  <td>
                    <div className="flex gap6">
                      <input className="input-tiny" type="number" min="0" max={insumo.contenidoEnvase} step="1" inputMode="numeric"
                             aria-label={`${unidad(insumo.unidadMedida)} que quedan en la abierta de ${insumo.nombre}`}
                             placeholder={String(Number(insumo.stockActual) % insumo.contenidoEnvase)} value={conteo.resto}
                             onChange={(e) => cambiar(insumo.id, 'resto', e.target.value)} />
                      <span className="tiny muted">{unidad(insumo.unidadMedida)}</span>
                    </div>
                  </td>
                  <td className="right">{valido ? cantidad(fisico, insumo.unidadMedida) : '—'}</td>
                  <td className={`right ${diferencia < 0 ? 'text-red' : diferencia > 0 ? 'text-green' : ''}`}>
                    {valido ? `${conSigno(diferencia)} ${unidad(insumo.unidadMedida)}` : '—'}
                    {valido && diferencia !== 0 && (
                      <div className="record-sub">≈ {envasesDecimal(diferencia, insumo.contenidoEnvase, insumo.unidadMedida)}</div>
                    )}
                  </td>
                  <td className="right">{valido ? pesos(merma) : '—'}</td>
                </tr>
                {cargado && (
                  <tr className="fila-comentario">
                    <td colSpan={7}>
                      <input maxLength={200} aria-label={`Comentario sobre ${insumo.nombre}`} value={conteo.comentario}
                             onChange={(e) => cambiar(insumo.id, 'comentario', e.target.value)}
                             placeholder={diferencia ? 'Justificá la diferencia: se rompió una botella, tragos de cortesía (indicá qué bartender)…'
                               : 'Comentario (opcional)'} />
                      {!valido && (
                        <small className="text-red">Revisá los números: en la abierta puede quedar hasta {cantidad(insumo.contenidoEnvase, insumo.unidadMedida)}.</small>
                      )}
                    </td>
                  </tr>
                )}
              </Fragment>
            ))}
          </tbody>
        </table>
      </div>
      <div className="audit-foot">
        <div>
          <div className="kpi-label">Merma preliminar</div>
          <div className="price text-red">{pesos(mermaPreliminar)}</div>
          <div className="tiny muted">{contados.length ? `${contados.length} insumo(s) con recuento` : 'Sin recuentos cargados'}</div>
        </div>
        <button className="btn btn-primary" type="button" disabled={!contados.length || conError || ocupado} onClick={registrar}>
          Registrar cierre y ajustar stock →
        </button>
      </div>
    </Tarjeta>
  );
}

export function HistorialAuditorias({ auditorias, detallado = false }) {
  if (!auditorias.length) return <Vacio>Todavía no se registraron cierres.</Vacio>;
  return auditorias.map((a) => (
    <details className="detalle-auditoria record" key={a.id} style={{ display: 'block' }} open={false}>
      <summary className="flex between">
        <div>
          <div className="record-label">Cierre #{a.numero} · {fechaHora(a.fecha)}</div>
          <div className="record-sub">{a.responsable} · {a.detalles.length} insumo(s) · {a.diferencias} diferencia(s)</div>
        </div>
        <b className={Number(a.mermaTotal) ? 'text-red' : ''}>{pesos(a.mermaTotal)}</b>
      </summary>
      {detallado && (
        <div className="table-wrap mt8">
          <table>
            <thead><tr><th>Insumo</th><th className="right">Teórico</th><th className="right">Físico</th><th className="right">Diferencia</th><th className="right">Merma</th><th>Comentario</th></tr></thead>
            <tbody>
              {a.detalles.map((d) => (
                <tr key={d.insumoId}>
                  <td>{d.insumo}</td>
                  <td className="right">{cantidad(d.stockTeorico, d.unidadMedida)}<div className="record-sub">{enEnvases(d.stockTeorico, d.contenidoEnvase, d.unidadMedida)}</div></td>
                  <td className="right">{cantidad(d.stockFisico, d.unidadMedida)}<div className="record-sub">{enEnvases(d.stockFisico, d.contenidoEnvase, d.unidadMedida)}</div></td>
                  <td className={`right ${Number(d.diferencia) < 0 ? 'text-red' : ''}`}>{conSigno(d.diferencia)} {unidad(d.unidadMedida)}</td>
                  <td className="right">{pesos(d.costoMerma)}</td>
                  <td className="texto-largo">{d.comentario || <span className="muted">—</span>}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </details>
  ));
}

export function ListaNotificaciones({ notificaciones, limite }) {
  if (!notificaciones.length) return <Vacio>No se dispararon alertas todavía.</Vacio>;
  const icono = { PANEL: '🖥', EMAIL: '✉', SMS: '💬', WHATSAPP: '🟢' };
  return notificaciones.slice(0, limite).map((n) => (
    <div className="alert" key={n.id}>
      <h4>⚠ Stock bajo · {fechaHora(n.fecha)} <span className="tag">{icono[n.canal]} {n.canal}</span></h4>
      <p>{n.mensaje}</p>
      {n.canal !== 'PANEL' && <p className="tiny">{n.resultado === 'SIMULADA' ? 'Envío de prueba' : 'No se pudo enviar'} a {n.destinatario}</p>}
    </div>
  ));
}

export const PillCanal = ({ canal }) => <Pill tono="info">{canal}</Pill>;
