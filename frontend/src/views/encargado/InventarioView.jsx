import { useState } from 'react';
import { api } from '../../api/client.js';
import { useAccion } from '../../components/Avisos.jsx';
import { cantidad, enEnvases, nombreEnvase, pesos, pesosConDecimales, unidad } from '../../components/formato.js';
import { InsumoModal } from '../../components/insumos.jsx';
import { AlertasActivas, KpisStock, Movimientos, TablaStock } from '../../components/inventario.jsx';
import { Cargando, Encabezado, Modal, Pill, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

function CompraModal({ insumo, onCerrar, onComprar, ocupado }) {
  const [envases, setEnvases] = useState(1);
  const envase = insumo ? nombreEnvase(insumo.unidadMedida, false) : '';
  return (
    <Modal abierto={Boolean(insumo)} titulo={insumo ? `Reponer ${insumo.nombre}` : ''} onCerrar={onCerrar} ocupado={ocupado}
           textoEnviar="Registrar compra" onEnviar={() => onComprar(insumo, Number(envases))}>
      {insumo && (
        <>
          <div className="note info mb14">
            Cada {envase} agrega <b>{cantidad(insumo.contenidoEnvase, insumo.unidadMedida)}</b> y cuesta <b>{pesos(insumo.costoEnvase)}</b>.
            Hoy hay {enEnvases(insumo.stockActual, insumo.contenidoEnvase, insumo.unidadMedida)}.
          </div>
          <div className="field">
            <label htmlFor="envases">Cantidad de {nombreEnvase(insumo.unidadMedida)}</label>
            <input id="envases" type="number" min="1" max="500" step="1" required value={envases}
                   onChange={(e) => setEnvases(e.target.value)} />
            <small>Total: {cantidad(insumo.contenidoEnvase * (Number(envases) || 0), insumo.unidadMedida)} · {pesos(insumo.costoEnvase * (Number(envases) || 0))}</small>
          </div>
        </>
      )}
    </Modal>
  );
}

/** Operación diaria del encargado: compras, edición y bajas. Los insumos nuevos los crea el dueño. */
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
      (m) => `Compra registrada: ${envases} ${nombreEnvase(insumo.unidadMedida, envases !== 1)} de ${insumo.nombre} (+${cantidad(m.cantidad, m.unidadMedida)})`)) {
      setComprando(null);
      refrescar();
    }
  };

  const guardar = async (datos) => {
    if (await ejecutar(() => api.insumos.editar(establecimientoId, editando.id, datos), 'Insumo guardado')) {
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
                  descripcion={`Insumos de ${establecimiento.nombre}: stock en mililitros y en botellas, costos y puntos de reposición. Las compras ingresan botellas completas.`} />
      <Cargando estado={insumos}>
        {(lista) => (
          <>
            <KpisStock insumos={lista} />
            <Tarjeta titulo="Stock disponible" subtitulo="Comprá botellas para reponer; cada movimiento queda registrado."
                     extra={<Pill tono="info">ml y botellas</Pill>}>
              <TablaStock insumos={lista} acciones={(i) => (
                <>
                  <button className="btn btn-small" type="button" onClick={() => setComprando(i)}>+ Envase</button>
                  <button className="btn btn-small btn-ghost" type="button" onClick={() => setEditando(i)}>Editar</button>
                  <button className="btn btn-small btn-ghost" type="button" onClick={() => baja(i)} aria-label={`Dar de baja ${i.nombre}`}>Baja</button>
                </>
              )} />
              <div className="note info mt12">¿Falta un insumo en la lista? Los insumos nuevos y su stock inicial los carga el dueño.</div>
            </Tarjeta>
            <div className="cols-half mt16">
              <Tarjeta titulo="Movimientos de inventario" subtitulo="Compras, consumos por despacho y ajustes por recuento">
                <Movimientos movimientos={movimientos.datos || []} />
              </Tarjeta>
              <div>
                <Tarjeta titulo="Alertas activas" subtitulo="Según el punto de reposición de cada insumo">
                  <AlertasActivas insumos={lista} />
                </Tarjeta>
                <Tarjeta titulo="Insumos dados de baja" subtitulo="Se pueden reactivar; conservan su historial">
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
      {editando && (
        <InsumoModal key={editando.id} abierto insumo={editando} ocupado={ocupado} onCerrar={() => setEditando(null)} onGuardar={guardar} />
      )}
    </>
  );
}
