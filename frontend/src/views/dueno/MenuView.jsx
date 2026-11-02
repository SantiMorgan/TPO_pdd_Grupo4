import { useState } from 'react';
import { api } from '../../api/client.js';
import { useAccion } from '../../components/Avisos.jsx';
import { ingredientesTexto, pesos, unidad } from '../../components/formato.js';
import { Cargando, Encabezado, Modal, Pill, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

const ICONOS = ['🍹', '🍸', '🥃', '🍷', '🥤', '🍺', '🍾', '🍋'];

function RecetaModal({ abierto, receta, insumos, onCerrar, onGuardar, ocupado }) {
  const [datos, setDatos] = useState(() => ({
    nombre: receta?.nombre ?? '', descripcion: receta?.descripcion ?? '', icono: receta?.icono ?? '🍹',
    precioBase: receta?.precioBase ?? 7000,
    ingredientes: receta?.ingredientes.map((i) => ({ insumoId: i.insumoId, cantidad: i.cantidad }))
      ?? (insumos[0] ? [{ insumoId: insumos[0].id, cantidad: 50 }] : []),
  }));
  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));
  const cambiarIngrediente = (posicion, campo, valor) => setDatos((d) => ({
    ...d, ingredientes: d.ingredientes.map((ing, i) => (i === posicion ? { ...ing, [campo]: valor } : ing)),
  }));
  const quitar = (posicion) => setDatos((d) => ({ ...d, ingredientes: d.ingredientes.filter((_, i) => i !== posicion) }));
  const agregar = () => {
    const libre = insumos.find((i) => !datos.ingredientes.some((ing) => ing.insumoId === i.id)) || insumos[0];
    if (libre) setDatos((d) => ({ ...d, ingredientes: [...d.ingredientes, { insumoId: libre.id, cantidad: 50 }] }));
  };

  return (
    <Modal abierto={abierto} titulo={receta ? `Editar ${receta.nombre}` : 'Nueva receta'} onCerrar={onCerrar} ocupado={ocupado}
           onEnviar={() => onGuardar({
             nombre: datos.nombre, descripcion: datos.descripcion, icono: datos.icono, precioBase: Number(datos.precioBase),
             ingredientes: datos.ingredientes.map((i) => ({ insumoId: i.insumoId, cantidad: Number(i.cantidad) })),
           })}>
      <div className="fieldrow">
        <div className="field"><label htmlFor="rnombre">Nombre del trago</label>
          <input id="rnombre" required maxLength={80} value={datos.nombre} onChange={cambiar('nombre')} /></div>
        <div className="field"><label htmlFor="rprecio">Precio base (ARS)</label>
          <input id="rprecio" type="number" min="0.01" step="0.01" required value={datos.precioBase} onChange={cambiar('precioBase')} /></div>
      </div>
      <div className="fieldrow mt12">
        <div className="field"><label htmlFor="rdesc">Descripción para la carta</label>
          <input id="rdesc" maxLength={200} value={datos.descripcion} onChange={cambiar('descripcion')} /></div>
        <div className="field"><label htmlFor="ricono">Ícono</label>
          <select id="ricono" value={datos.icono} onChange={cambiar('icono')}>{ICONOS.map((i) => <option key={i} value={i}>{i}</option>)}</select></div>
      </div>
      <div className="flex between mt16 mb8">
        <div className="strong small">Ingredientes por unidad</div>
        <button className="btn btn-small" type="button" onClick={agregar} disabled={!insumos.length}>+ Ingrediente</button>
      </div>
      {datos.ingredientes.map((ing, i) => {
        const insumo = insumos.find((x) => x.id === ing.insumoId);
        return (
          <div className="partrow" key={i}>
            <select aria-label="Insumo" value={ing.insumoId} onChange={(e) => cambiarIngrediente(i, 'insumoId', e.target.value)}>
              {insumos.map((x) => <option key={x.id} value={x.id}>{x.nombre}</option>)}
            </select>
            <input aria-label={`Cantidad en ${unidad(insumo?.unidadMedida)}`} type="number" min="1" step="1" required
                   value={ing.cantidad} onChange={(e) => cambiarIngrediente(i, 'cantidad', e.target.value)} />
            <button type="button" onClick={() => quitar(i)} disabled={datos.ingredientes.length <= 1} aria-label="Quitar ingrediente">×</button>
          </div>
        );
      })}
      <div className="note info mt12">El costo teórico lo calcula la receta con el costo actual de cada insumo (Experto en información).</div>
    </Modal>
  );
}

export default function MenuView() {
  const { establecimientoId, establecimiento } = useSesion();
  const recetas = useDatos(() => api.recetas.listar(establecimientoId, true), [establecimientoId]);
  const insumos = useDatos(() => api.insumos.listar(establecimientoId), [establecimientoId]);
  const [editando, setEditando] = useState(null);
  const [ejecutar, ocupado] = useAccion();

  const guardar = async (datos) => {
    const nueva = editando === 'nueva';
    const ok = await ejecutar(() => (nueva
      ? api.recetas.crear(establecimientoId, datos)
      : api.recetas.editar(establecimientoId, editando.id, datos)), 'Receta guardada');
    if (ok) {
      setEditando(null);
      recetas.recargar();
    }
  };

  const alternar = async (receta) => {
    const accion = receta.activa ? api.recetas.quitarDelMenu : api.recetas.volverAlMenu;
    if (await ejecutar(() => accion(establecimientoId, receta.id),
      receta.activa ? `${receta.nombre} salió del menú` : `${receta.nombre} volvió al menú`)) recetas.recargar();
  };

  return (
    <>
      <Encabezado titulo="Menú y recetas"
                  descripcion={`Los tragos de ${establecimiento.nombre} se arman con los insumos del bar. Cada receta define los mililitros que se descuentan al despachar.`}
                  extra={<button className="btn btn-primary" type="button" disabled={!insumos.datos?.length} onClick={() => setEditando('nueva')}>+ Nueva receta</button>} />
      <Tarjeta titulo="Recetas" subtitulo="Quitar del menú es una baja lógica: las comandas anteriores conservan su detalle."
               extra={<Pill>{recetas.datos?.length ?? 0} recetas</Pill>}>
        <Cargando estado={recetas}>
          {(lista) => (lista.length ? lista.map((r) => {
            const contribucion = Number(r.precioBase) - Number(r.costo);
            return (
              <div className="config-item" key={r.id}>
                <div style={{ minWidth: 0 }}>
                  <div className="record-label">{r.icono} {r.nombre} {!r.activa && <Pill>Fuera del menú</Pill>} {r.activa && !r.disponible && <Pill tono="danger">Sin stock</Pill>}</div>
                  <div className="record-sub">{ingredientesTexto(r.ingredientes)}</div>
                  <div className="small">
                    <b>{pesos(r.precioBase)}</b> <span className="muted">base · costo {pesos(r.costo)} · contribución {pesos(contribucion)}
                      {Number(r.precioBase) ? ` (${Math.round((contribucion / Number(r.precioBase)) * 100)} %)` : ''}</span>
                  </div>
                </div>
                <div className="flex gap6">
                  <button className="btn btn-small" type="button" onClick={() => setEditando(r)}>Editar</button>
                  <button className={`btn btn-small ${r.activa ? 'btn-danger' : 'btn-green'}`} type="button" disabled={ocupado} onClick={() => alternar(r)}>
                    {r.activa ? 'Quitar del menú' : 'Volver al menú'}
                  </button>
                </div>
              </div>
            );
          }) : <Vacio simbolo="🍹">Todavía no hay recetas. {insumos.datos?.length ? '' : 'Primero el encargado tiene que cargar insumos.'}</Vacio>)}
        </Cargando>
      </Tarjeta>
      {insumos.datos && (
        <RecetaModal key={editando === 'nueva' ? 'nueva' : editando?.id || 'cerrado'} abierto={Boolean(editando)}
                     receta={editando === 'nueva' ? null : editando} insumos={insumos.datos} ocupado={ocupado}
                     onCerrar={() => setEditando(null)} onGuardar={guardar} />
      )}
    </>
  );
}
