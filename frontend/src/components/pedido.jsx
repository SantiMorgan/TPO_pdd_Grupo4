import { useCallback, useMemo, useState } from 'react';
import { ingredientesTexto, pesos } from './formato.js';
import { Pill, Tarjeta, Vacio } from './ui.jsx';

/** Estado del carrito: solo ids y cantidades; el precio real lo fija el backend al registrar. */
export function usePedido() {
  const [items, setItems] = useState([]);
  const agregar = useCallback((recetaId) => setItems((actuales) => {
    const existente = actuales.find((i) => i.recetaId === recetaId);
    if (existente) return actuales.map((i) => (i === existente ? { ...i, cantidad: Math.min(99, i.cantidad + 1) } : i));
    return [...actuales, { recetaId, cantidad: 1 }];
  }), []);
  const quitar = useCallback((recetaId) => setItems((actuales) => actuales
    .map((i) => (i.recetaId === recetaId ? { ...i, cantidad: i.cantidad - 1 } : i))
    .filter((i) => i.cantidad > 0)), []);
  const vaciar = useCallback(() => setItems([]), []);
  const unidades = items.reduce((total, i) => total + i.cantidad, 0);
  return { items, agregar, quitar, vaciar, unidades };
}

export function MenuTragos({ items, onAgregar, mostrarCosto = false }) {
  if (!items.length) return <Vacio simbolo="🍸">Este bar todavía no tiene tragos en el menú.</Vacio>;
  return (
    <div className="drink-grid">
      {items.map((item) => {
        const conDescuento = Number(item.precio) !== Number(item.precioBase);
        return (
          <button key={item.recetaId} type="button" className={`drink${item.disponible ? '' : ' agotado'}`}
                  disabled={!item.disponible} onClick={() => onAgregar(item.recetaId)}
                  title={item.disponible ? `Agregar ${item.nombre}` : 'Sin stock suficiente en este momento'}>
            <div className="flex between">
              <div className="drink-emoji">{item.icono}</div>
              {!item.disponible && <Pill tono="danger">Agotado</Pill>}
            </div>
            <div className="drink-name">{item.nombre}</div>
            <div className="drink-parts">{item.descripcion || ingredientesTexto(item.ingredientes)}</div>
            {item.descripcion && <div className="tiny muted mt8">{ingredientesTexto(item.ingredientes)}</div>}
            <div className="drink-foot">
              <div>
                {conDescuento && <span className="precio-base">{pesos(item.precioBase)}</span>}
                <b className="price">{pesos(item.precio)}</b>
                {mostrarCosto && <div className="tiny muted">Costo {pesos(item.costo)}</div>}
              </div>
              <span className="plus">+</span>
            </div>
          </button>
        );
      })}
    </div>
  );
}

export function Carrito({ titulo, subtitulo, carta, pedido, onEnviar, textoEnviar, ocupado, children, nota }) {
  const precios = useMemo(() => new Map((carta?.items || []).map((i) => [i.recetaId, i])), [carta]);
  const lineas = pedido.items.map((i) => ({ ...i, receta: precios.get(i.recetaId) })).filter((l) => l.receta);
  const total = lineas.reduce((suma, l) => suma + Number(l.receta.precio) * l.cantidad, 0);
  return (
    <Tarjeta titulo={titulo} subtitulo={subtitulo} extra={<Pill tono="info">{pedido.unidades} unidades</Pill>}>
      {lineas.length ? (
        <>
          {lineas.map((l) => (
            <div className="order-line" key={l.recetaId}>
              <div style={{ flex: 1, minWidth: 0 }}>
                <div className="record-label">{l.receta.icono} {l.receta.nombre}</div>
                <div className="record-sub">{pesos(l.receta.precio)} c/u</div>
              </div>
              <div className="qty">
                <button type="button" onClick={() => pedido.quitar(l.recetaId)} aria-label={`Restar ${l.receta.nombre}`}>−</button>
                <b>{l.cantidad}</b>
                <button type="button" onClick={() => pedido.agregar(l.recetaId)} aria-label={`Sumar ${l.receta.nombre}`}>+</button>
              </div>
              <b className="small">{pesos(Number(l.receta.precio) * l.cantidad)}</b>
            </div>
          ))}
          <div className="flex between mt16">
            <div className="muted small">Total estimado</div>
            <b className="price">{pesos(total)}</b>
          </div>
          {children}
          <button className="btn btn-primary mt16" style={{ width: '100%' }} type="button" disabled={ocupado} onClick={onEnviar}>
            {textoEnviar}
          </button>
          {nota && <div className="tiny muted mt8">{nota}</div>}
        </>
      ) : (
        <Vacio simbolo="🍸">Elegí al menos un trago del menú.</Vacio>
      )}
    </Tarjeta>
  );
}
