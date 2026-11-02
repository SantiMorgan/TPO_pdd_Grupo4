import { useState } from 'react';
import { api } from '../../api/client.js';
import { useAccion } from '../../components/Avisos.jsx';
import { cantidad, pesos, pesosConDecimales, unidad } from '../../components/formato.js';
import { AlertasActivas, KpisStock, Movimientos, TablaStock } from '../../components/inventario.jsx';
import { Cargando, Encabezado, Modal, Pill, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

function CompraModal({ insumo, onCerrar, onComprar, ocupado }) {
  const [envases, setEnvases] = useState(1);
  return (
    <Modal abierto={Boolean(insumo)} titulo={insumo ? `Reponer ${insumo.nombre}` : ''} onCerrar={onCerrar} ocupado={ocupado}
           textoEnviar="Registrar compra" onEnviar={() => onComprar(insumo, Number(envases))}>
      {insumo && (
        <>
          <div className="note info mb14">
            Cada envase agrega <b>{cantidad(insumo.contenidoEnvase, insumo.unidadMedida)}</b> y cuesta <b>{pesos(insumo.costoEnvase)}</b>.
          </div>
          <div className="field">
            <label htmlFor="envases">Cantidad de envases</label>
            <input id="envases" type="number" min="1" max="500" step="1" required value={envases}
                   onChange={(e) => setEnvases(e.target.value)} />
            <small>Total: {cantidad(insumo.contenidoEnvase * (Number(envases) || 0), insumo.unidadMedida)} · {pesos(insumo.costoEnvase * (Number(envases) || 0))}</small>
          </div>
        </>
      )}
    </Modal>
  );
}

function InsumoModal({ abierto, insumo, onCerrar, onGuardar, ocupado }) {
  const nuevo = !insumo;
  const [datos, setDatos] = useState(() => ({
    codigo: '', nombre: insumo?.nombre ?? '', unidadMedida: insumo?.unidadMedida ?? 'ML',
    contenidoEnvase: insumo?.contenidoEnvase ?? 750, costoUnitario: insumo?.costoUnitario ?? '',
    puntoReorden: insumo?.puntoReorden ?? 250, stockInicial: 750,
  }));
  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));
  const costoEnvase = Number(datos.costoUnitario || 0) * Number(datos.contenidoEnvase || 0);
  return (
    <Modal abierto={abierto} titulo={nuevo ? 'Agregar insumo' : `Editar ${insumo.nombre}`} onCerrar={onCerrar} ocupado={ocupado}
           onEnviar={() => onGuardar(datos)}>
      <div className="fieldrow">
        {nuevo && (
          <div className="field"><label htmlFor="codigo">Código</label>
            <input id="codigo" required maxLength={50} value={datos.codigo} onChange={cambiar('codigo')} placeholder="Ej.: CAMPARI" /></div>
        )}
        <div className="field"><label htmlFor="nombre">Nombre</label>
          <input id="nombre" required maxLength={150} value={datos.nombre} onChange={cambiar('nombre')} /></div>
        {nuevo && (
          <div className="field"><label htmlFor="unidad">Unidad de medida</label>
            <select id="unidad" value={datos.unidadMedida} onChange={cambiar('unidadMedida')}>
              <option value="ML">Mililitros (ml)</option><option value="UNIDAD">Unidades</option>
            </select></div>
        )}
        <div className="field"><label htmlFor="contenido">Contenido por envase ({unidad(datos.unidadMedida)})</label>
          <input id="contenido" type="number" min="1" step="1" required value={datos.contenidoEnvase} onChange={cambiar('contenidoEnvase')} /></div>
        <div className="field"><label htmlFor="costo">Costo por {unidad(datos.unidadMedida)} (ARS)</label>
          <input id="costo" type="number" min="0" step="0.01" required value={datos.costoUnitario} onChange={cambiar('costoUnitario')} />
          <small>Envase ≈ {pesos(costoEnvase)}</small></div>
        <div className="field"><label htmlFor="reorden">Punto de reposición ({unidad(datos.unidadMedida)})</label>
          <input id="reorden" type="number" min="0" step="1" required value={datos.puntoReorden} onChange={cambiar('puntoReorden')} /></div>
        {nuevo && (
          <div className="field"><label htmlFor="stock">Stock inicial ({unidad(datos.unidadMedida)})</label>
            <input id="stock" type="number" min="0" step="1" required value={datos.stockInicial} onChange={cambiar('stockInicial')} /></div>
        )}
      </div>
      {!nuevo && <div className="note info mt12">El stock no se edita desde acá: usá Compras o la Auditoría para que quede registrado.</div>}
    </Modal>
  );
}

export default function InventarioView() {
  const { establecimientoId, establecimiento } = useSesion();
  const insumos = useDatos(() => api.insumos.listar(establecimientoId), [establecimientoId], { intervalo: 6000 });
  const inactivos = useDatos(() => api.insumos.listar(establecimientoId, false), [establecimientoId]);
  const movimientos = useDatos(() => api.movimientos(establecimientoId, 10), [establecimientoId], { intervalo: 6000 });
  const [comprando, setComprando] = useState(null);
  const [editando, setEditando] = useState(null);
  const [ejecutar, ocupado] = useAccion();

  const refrescar = () => { insumos.recargar(); inactivos.recargar(); movimientos.recargar(); };

  const comprar = async (insumo, envases) => {
    if (await ejecutar(() => api.comprar(establecimientoId, insumo.id, envases),
      (m) => `Compra registrada: ${envases} envase(s) de ${insumo.nombre} (+${cantidad(m.cantidad, m.unidadMedida)})`)) {
      setComprando(null);
      refrescar();
    }
  };

  const guardar = async (datos) => {
    const nuevo = editando === 'nuevo';
    const cuerpo = {
      nombre: datos.nombre, costoUnitario: Number(datos.costoUnitario), puntoReorden: Number(datos.puntoReorden),
      contenidoEnvase: Number(datos.contenidoEnvase),
    };
    const ok = await ejecutar(() => (nuevo
      ? api.insumos.crear(establecimientoId, { ...cuerpo, codigo: datos.codigo, unidadMedida: datos.unidadMedida, stockInicial: Number(datos.stockInicial) })
      : api.insumos.editar(establecimientoId, editando.id, cuerpo)), 'Insumo guardado');
    if (ok) {
      setEditando(null);
      refrescar();
    }
  };

  const baja = async (insumo) => {
    if (!window.confirm(`¿Dar de baja ${insumo.nombre}? Las recetas que lo usan quedan no disponibles.`)) return;
    if (await ejecutar(() => api.insumos.darDeBaja(establecimientoId, insumo.id), `${insumo.nombre} dado de baja`)) refrescar();
  };

  const alta = async (insumo) => {
    if (await ejecutar(() => api.insumos.darDeAlta(establecimientoId, insumo.id), `${insumo.nombre} reactivado`)) refrescar();
  };

  return (
    <>
      <Encabezado titulo="Inventario y compras"
                  descripcion={`Insumos de ${establecimiento.nombre}: costos, stock fraccionado y puntos de reposición. Las compras ingresan envases completos.`}
                  extra={<button className="btn btn-primary" type="button" onClick={() => setEditando('nuevo')}>+ Nuevo insumo</button>} />
      <Cargando estado={insumos}>
        {(lista) => (
          <>
            <KpisStock insumos={lista} />
            <Tarjeta titulo="Stock disponible" subtitulo="Comprá envases para reponer; cada movimiento queda registrado."
                     extra={<Pill tono="info">Unidad base: ml</Pill>}>
              <TablaStock insumos={lista} acciones={(i) => (
                <>
                  <button className="btn btn-small" type="button" onClick={() => setComprando(i)}>+ Envase</button>
                  <button className="btn btn-small btn-ghost" type="button" onClick={() => setEditando(i)}>Editar</button>
                  <button className="btn btn-small btn-ghost" type="button" onClick={() => baja(i)} aria-label={`Dar de baja ${i.nombre}`}>Baja</button>
                </>
              )} />
            </Tarjeta>
            <div className="cols-half mt16">
              <Tarjeta titulo="Movimientos de inventario" subtitulo="Compras, consumos por despacho y ajustes de auditoría">
                <Movimientos movimientos={movimientos.datos || []} />
              </Tarjeta>
              <div>
                <Tarjeta titulo="Alertas activas" subtitulo="Según el punto de reposición de cada insumo">
                  <AlertasActivas insumos={lista} />
                </Tarjeta>
                <Tarjeta titulo="Insumos dados de baja" subtitulo="Baja lógica: conservan su historial">
                  {(inactivos.datos || []).length ? inactivos.datos.map((i) => (
                    <div className="record" key={i.id}>
                      <div><div className="record-label">{i.nombre}</div><div className="record-sub">{i.codigo} · {pesosConDecimales(i.costoUnitario)}/{unidad(i.unidadMedida)}</div></div>
                      <button className="btn btn-small" type="button" onClick={() => alta(i)}>Reactivar</button>
                    </div>
                  )) : <Vacio>No hay insumos dados de baja.</Vacio>}
                </Tarjeta>
              </div>
            </div>
          </>
        )}
      </Cargando>
      <CompraModal key={comprando?.id} insumo={comprando} ocupado={ocupado} onCerrar={() => setComprando(null)} onComprar={comprar} />
      <InsumoModal key={editando === 'nuevo' ? 'nuevo' : editando?.id || 'cerrado'} abierto={Boolean(editando)}
                   insumo={editando === 'nuevo' ? null : editando} ocupado={ocupado} onCerrar={() => setEditando(null)} onGuardar={guardar} />
    </>
  );
}
