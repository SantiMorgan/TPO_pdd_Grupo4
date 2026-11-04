import { useEffect, useRef } from 'react';
import { useSesion } from '../session/Sesion.jsx';

export function Encabezado({ titulo, descripcion, extra }) {
  const { establecimiento } = useSesion();
  return (
    <div className="page-head">
      <div className="eyebrow">{establecimiento ? `${establecimiento.nombre} · ${establecimiento.zona}` : 'BarStock Solutions'}</div>
      <div className="flex between wrap gap12">
        <div>
          <h1>{titulo}</h1>
          {descripcion && <div className="page-desc mb0">{descripcion}</div>}
        </div>
        {extra}
      </div>
    </div>
  );
}

export function Tarjeta({ titulo, subtitulo, extra, children, className = '' }) {
  return (
    <section className={`card ${className}`}>
      {(titulo || extra) && (
        <div className="card-head">
          <div>
            {titulo && <h2>{titulo}</h2>}
            {subtitulo && <p>{subtitulo}</p>}
          </div>
          {extra}
        </div>
      )}
      {children}
    </section>
  );
}

export const Pill = ({ tono, children }) => <span className={`pill${tono ? ` p-${tono}` : ''}`}>{children}</span>;

export function Kpi({ etiqueta, valor, detalle, tono, icono }) {
  return (
    <div className="card">
      <div className="flex between">
        <div className="kpi-label">{etiqueta}</div>
        {icono && <span className="kpi-icon">{icono}</span>}
      </div>
      <div className={`kpi-value${tono === 'warn' ? ' kpi-warn' : tono === 'accent' ? ' kpi-accent' : ''}`}>{valor}</div>
      {detalle && <div className="kpi-detail">{detalle}</div>}
    </div>
  );
}

export function Vacio({ simbolo, children }) {
  return (
    <div className="empty">
      {simbolo && <span className="empty-symbol">{simbolo}</span>}
      {children}
    </div>
  );
}

export function EstadoStock({ insumo }) {
  const estado = !insumo.activo ? 'Baja'
    : Number(insumo.stockActual) <= Number(insumo.puntoReorden) ? 'Crítico'
      : Number(insumo.stockActual) <= Number(insumo.puntoReorden) * 1.65 ? 'Atención' : 'Normal';
  const tono = { Baja: undefined, Crítico: 'danger', Atención: 'warn', Normal: 'success' }[estado];
  return <Pill tono={tono}>{estado}</Pill>;
}

export function Nivel({ insumo }) {
  const ancho = Math.max(0, Math.min(100, (Number(insumo.stockActual) / Math.max(1, insumo.contenidoEnvase * 2)) * 100));
  return (
    <div className="level" aria-hidden="true">
      <span className={insumo.necesitaReposicion ? 'low' : ''} style={{ width: `${ancho}%` }} />
    </div>
  );
}

export function Cargando({ estado, children }) {
  if (estado.error && !estado.datos) return <div className="note danger">{estado.error.message}</div>;
  if (!estado.datos) return <Vacio>Cargando…</Vacio>;
  return children(estado.datos);
}

/** Diálogo del prototipo con <dialog>: cierra con Escape, con la × o haciendo clic afuera. */
export function Modal({ abierto, titulo, onCerrar, onEnviar, textoEnviar = 'Guardar', ocupado, children }) {
  const ref = useRef(null);
  useEffect(() => {
    const dialogo = ref.current;
    if (!dialogo) return;
    if (abierto && !dialogo.open) dialogo.showModal();
    if (!abierto && dialogo.open) dialogo.close();
  }, [abierto]);

  return (
    <dialog ref={ref} className="dialog" onClose={onCerrar} onClick={(e) => e.target === ref.current && onCerrar()}>
      {abierto && (
        <div className="dialog-body">
          <div className="dialog-head">
            <div>
              <div className="eyebrow">BarStock Solutions</div>
              <h2>{titulo}</h2>
            </div>
            <button className="icon-close" type="button" onClick={onCerrar} aria-label="Cerrar">×</button>
          </div>
          <form onSubmit={(e) => { e.preventDefault(); onEnviar?.(new FormData(e.currentTarget)); }}>
            <div>{children}</div>
            <div className="actions-row mt16">
              <button className="btn" type="button" onClick={onCerrar}>{onEnviar ? 'Cancelar' : 'Cerrar'}</button>
              {onEnviar && <button className="btn btn-primary" type="submit" disabled={ocupado}>{textoEnviar}</button>}
            </div>
          </form>
        </div>
      )}
    </dialog>
  );
}
