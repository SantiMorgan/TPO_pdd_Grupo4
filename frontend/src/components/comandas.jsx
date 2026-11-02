import { useState } from 'react';
import { hora, pesos } from './formato.js';
import { Modal, Pill } from './ui.jsx';

const TONO_ESTADO = { REGISTRADA: 'info', EN_PREPARACION: 'warn', DESPACHADA: 'success', ANULADA: 'danger' };

export const PillEstado = ({ comanda }) => <Pill tono={TONO_ESTADO[comanda.estado]}>{comanda.estadoNombre}</Pill>;

export const itemsTexto = (comanda) => comanda.items
  .map((i) => `${i.cantidad}× ${i.nombre}${i.cantidadDevuelta ? ` (${i.cantidadDevuelta} devuelto${i.cantidadDevuelta > 1 ? 's' : ''})` : ''}`)
  .join(' · ');

/** Total registrado y, si hubo devoluciones, lo que queda cobrado. */
export function TotalComanda({ comanda }) {
  const devuelto = Number(comanda.totalDevuelto) > 0;
  return (
    <div className="small">
      {devuelto && <span className="precio-base">{pesos(comanda.total)}</span>}
      <b>{pesos(devuelto ? comanda.totalNeto : comanda.total)}</b> <span className="muted">· {comanda.estrategiaPrecio}</span>
      {devuelto && <span className="muted"> · se devolvieron {pesos(comanda.totalDevuelto)}</span>}
    </div>
  );
}

export function Devoluciones({ comanda }) {
  if (!comanda.devoluciones?.length) return null;
  return (
    <ul className="devoluciones">
      {comanda.devoluciones.map((d, i) => (
        <li key={`${d.itemId}-${i}`}>↩ {d.cantidad}× {d.trago} · {hora(d.fecha)} · {d.motivo}</li>
      ))}
    </ul>
  );
}

/**
 * Tarjeta de comanda del bartender. Los botones salen de {@code comanda.acciones},
 * que calcula el servidor según el estado actual: la vista no decide qué se puede hacer.
 */
export function TarjetaComanda({ comanda, onPreparar, onDespachar, onAnular, onDevolver, ocupado }) {
  const acciones = new Set(comanda.acciones);
  return (
    <div className="order">
      <div className="order-top">
        <div>
          <b className="strong">Comanda #{comanda.numero}</b>{' '}
          <span className="muted tiny">{hora(comanda.registradaEn)}</span>{' '}
          {comanda.origen === 'CLIENTE'
            ? <Pill tono="info">🙋 Cliente{comanda.cliente ? ` · ${comanda.cliente}` : ''}</Pill>
            : comanda.cliente && <Pill>{comanda.cliente}</Pill>}
        </div>
        <PillEstado comanda={comanda} />
      </div>
      <div className="order-items">{itemsTexto(comanda)}</div>
      <TotalComanda comanda={comanda} />
      <Devoluciones comanda={comanda} />
      {comanda.motivoAnulacion && !comanda.devoluciones?.length && <div className="record-sub">Motivo: {comanda.motivoAnulacion}</div>}
      {acciones.size > 0 && (
        <div className="order-actions">
          {acciones.has('PREPARAR') && (
            <button className="btn btn-small btn-dark" type="button" disabled={ocupado} onClick={() => onPreparar(comanda)}>▶ Iniciar preparación</button>
          )}
          {acciones.has('DESPACHAR') && (
            <button className="btn btn-small btn-green" type="button" disabled={ocupado} onClick={() => onDespachar(comanda)}>✓ Despachar y descontar</button>
          )}
          {acciones.has('ANULAR') && onAnular && (
            <button className="btn btn-small btn-danger" type="button" disabled={ocupado} onClick={() => onAnular(comanda)}>× Anular</button>
          )}
          {acciones.has('DEVOLVER') && onDevolver && (
            <button className="btn btn-small btn-danger" type="button" disabled={ocupado} onClick={() => onDevolver(comanda)}>↩ Devolver un trago</button>
          )}
        </div>
      )}
    </div>
  );
}

export function AnularModal({ comanda, onCerrar, onConfirmar, ocupado }) {
  const [motivo, setMotivo] = useState('');
  return (
    <Modal abierto={Boolean(comanda)} onCerrar={onCerrar} ocupado={ocupado}
           titulo={comanda ? `Anular la comanda #${comanda.numero}` : ''}
           textoEnviar="Confirmar" onEnviar={() => onConfirmar(comanda, motivo)}>
      <div className="note info mb14">No se descuenta ni se repone inventario: la comanda todavía no se despachó.</div>
      <div className="field">
        <label htmlFor="motivo">Motivo (obligatorio, al menos 5 caracteres)</label>
        <textarea id="motivo" rows={3} maxLength={220} required minLength={5} value={motivo}
                  onChange={(e) => setMotivo(e.target.value)} placeholder="Ej.: error de pedido, el cliente se fue…" />
      </div>
    </Modal>
  );
}

/** Devolución de algunas unidades de un trago, sin anular el resto de la comanda. */
export function DevolucionModal({ comanda, onCerrar, onConfirmar, ocupado }) {
  const disponibles = (comanda?.items || []).filter((i) => i.cantidad - i.cantidadDevuelta > 0);
  const [itemId, setItemId] = useState(disponibles[0]?.id || '');
  const [cantidad, setCantidad] = useState(1);
  const [motivo, setMotivo] = useState('');
  const item = disponibles.find((i) => i.id === itemId);
  const maximo = item ? item.cantidad - item.cantidadDevuelta : 1;

  return (
    <Modal abierto={Boolean(comanda)} onCerrar={onCerrar} ocupado={ocupado}
           titulo={comanda ? `Devolver un trago del pedido #${comanda.numero}` : ''} textoEnviar="Registrar devolución"
           onEnviar={() => onConfirmar(comanda, { itemId, cantidad: Number(cantidad), motivo })}>
      <div className="note mb14">
        Se descuenta el importe del trago devuelto; el resto del pedido sigue igual. Los insumos no vuelven al stock porque el trago ya se preparó.
      </div>
      <div className="fieldrow">
        <div className="field">
          <label htmlFor="trago">Trago</label>
          <select id="trago" value={itemId} onChange={(e) => { setItemId(e.target.value); setCantidad(1); }}>
            {disponibles.map((i) => (
              <option key={i.id} value={i.id}>{i.nombre} ({i.cantidad - i.cantidadDevuelta} de {i.cantidad}) · {pesos(i.precioUnitario)} c/u</option>
            ))}
          </select>
        </div>
        <div className="field">
          <label htmlFor="cantidadDevuelta">Cantidad</label>
          <input id="cantidadDevuelta" type="number" min="1" max={maximo} step="1" required value={cantidad}
                 onChange={(e) => setCantidad(e.target.value)} />
          <small>Se reintegran {pesos((item?.precioUnitario || 0) * (Number(cantidad) || 0))}</small>
        </div>
      </div>
      <div className="field mt12">
        <label htmlFor="motivoDevolucion">Motivo (obligatorio, al menos 5 caracteres)</label>
        <textarea id="motivoDevolucion" rows={3} maxLength={220} required minLength={5} value={motivo}
                  onChange={(e) => setMotivo(e.target.value)} placeholder="Ej.: estaba muy dulce, pidió otro trago…" />
      </div>
    </Modal>
  );
}
