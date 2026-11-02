import { useState } from 'react';
import { hora, pesos } from './formato.js';
import { Modal, Pill } from './ui.jsx';

const TONO_ESTADO = { REGISTRADA: 'info', EN_PREPARACION: 'warn', DESPACHADA: 'success', ANULADA: 'danger' };

export const PillEstado = ({ comanda }) => <Pill tono={TONO_ESTADO[comanda.estado]}>{comanda.estadoNombre}</Pill>;

export const itemsTexto = (comanda) => comanda.items.map((i) => `${i.cantidad}× ${i.nombre}`).join(' · ');

/**
 * Tarjeta de comanda del bartender. Los botones salen de {@code comanda.acciones},
 * que calcula el estado actual en el backend (patrón State): la vista no decide las transiciones.
 */
export function TarjetaComanda({ comanda, onPreparar, onDespachar, onAnular, ocupado }) {
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
      <div className="small"><b>{pesos(comanda.total)}</b> <span className="muted">· {comanda.estrategiaPrecio}</span></div>
      {comanda.motivoAnulacion && <div className="record-sub">Motivo: {comanda.motivoAnulacion}</div>}
      {acciones.size > 0 && (
        <div className="order-actions">
          {acciones.has('PREPARAR') && (
            <button className="btn btn-small btn-dark" type="button" disabled={ocupado} onClick={() => onPreparar(comanda)}>▶ Iniciar preparación</button>
          )}
          {acciones.has('DESPACHAR') && (
            <button className="btn btn-small btn-green" type="button" disabled={ocupado} onClick={() => onDespachar(comanda)}>✓ Despachar y descontar</button>
          )}
          {acciones.has('ANULAR') && onAnular && (
            <button className="btn btn-small btn-danger" type="button" disabled={ocupado} onClick={() => onAnular(comanda)}>
              {comanda.estado === 'DESPACHADA' ? '↩ Registrar devolución' : '× Anular'}
            </button>
          )}
        </div>
      )}
    </div>
  );
}

export function AnularModal({ comanda, onCerrar, onConfirmar, ocupado }) {
  const [motivo, setMotivo] = useState('');
  const devolucion = comanda?.estado === 'DESPACHADA';
  return (
    <Modal abierto={Boolean(comanda)} onCerrar={onCerrar} ocupado={ocupado}
           titulo={comanda ? (devolucion ? `Devolución de la comanda #${comanda.numero}` : `Anular la comanda #${comanda.numero}`) : ''}
           textoEnviar="Confirmar" onEnviar={() => onConfirmar(comanda, motivo)}>
      <div className={`note ${devolucion ? 'danger' : 'info'} mb14`}>
        {devolucion
          ? 'Se revierte el ingreso, pero NO se devuelven al inventario los mililitros ya consumidos.'
          : 'No se descuenta ni se repone inventario: el despacho todavía no ocurrió.'}
      </div>
      <div className="field">
        <label htmlFor="motivo">Motivo (obligatorio, al menos 5 caracteres)</label>
        <textarea id="motivo" rows={3} maxLength={220} required minLength={5} value={motivo}
                  onChange={(e) => setMotivo(e.target.value)} placeholder="Ej.: error de pedido, trago devuelto…" />
      </div>
    </Modal>
  );
}
