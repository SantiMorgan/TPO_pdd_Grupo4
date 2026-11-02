import { useState } from 'react';
import { puede } from '../routes/roles.js';
import { useSesion } from '../session/Sesion.jsx';
import { cantidad, enEnvases, envasesCerrados, nombreEnvase, pesos, unidad } from './formato.js';
import { Modal } from './ui.jsx';

/**
 * Alta y edición de insumos. El alta (con código y stock inicial) solo se muestra a los roles con
 * permiso para crear insumos; la edición no toca el stock, que cambia por compras o recuentos.
 */
export function InsumoModal({ abierto, insumo, onCerrar, onGuardar, ocupado }) {
  const { sesion } = useSesion();
  const nuevo = !insumo;
  const [datos, setDatos] = useState(() => ({
    codigo: insumo?.codigo ?? '', nombre: insumo?.nombre ?? '', unidadMedida: insumo?.unidadMedida ?? 'ML',
    contenidoEnvase: insumo?.contenidoEnvase ?? 750, costoUnitario: insumo?.costoUnitario ?? '',
    puntoReorden: insumo?.puntoReorden ?? 250, envasesIniciales: 1, restanteInicial: 0,
  }));
  if (nuevo && !puede(sesion.rol, 'CREAR_INSUMOS')) return null;

  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));
  const contenido = Number(datos.contenidoEnvase) || 0;
  const costoEnvase = Number(datos.costoUnitario || 0) * contenido;
  const stockInicial = (Number(datos.envasesIniciales) || 0) * contenido + (Number(datos.restanteInicial) || 0);

  const enviar = () => {
    const cuerpo = {
      nombre: datos.nombre, costoUnitario: Number(datos.costoUnitario), puntoReorden: Number(datos.puntoReorden),
      contenidoEnvase: contenido,
    };
    onGuardar(nuevo ? { ...cuerpo, codigo: datos.codigo, unidadMedida: datos.unidadMedida, stockInicial } : cuerpo);
  };

  return (
    <Modal abierto={abierto} titulo={nuevo ? 'Agregar insumo' : `Editar ${insumo.nombre}`} onCerrar={onCerrar} ocupado={ocupado}
           onEnviar={enviar}>
      <div className="fieldrow">
        <div className="field"><label htmlFor="codigo">Código</label>
          <input id="codigo" required={nuevo} disabled={!nuevo} maxLength={50} value={datos.codigo} onChange={cambiar('codigo')}
                 placeholder="Ej.: CAMPARI" />
          <small>{nuevo ? 'Identifica al insumo dentro del bar; no se puede repetir.' : 'El código no se cambia.'}</small></div>
        <div className="field"><label htmlFor="nombre">Nombre</label>
          <input id="nombre" required maxLength={150} value={datos.nombre} onChange={cambiar('nombre')} placeholder="Ej.: Campari" /></div>
        {nuevo && (
          <div className="field"><label htmlFor="unidad">Unidad de medida</label>
            <select id="unidad" value={datos.unidadMedida} onChange={cambiar('unidadMedida')}>
              <option value="ML">Mililitros (ml), en botellas</option><option value="UNIDAD">Unidades, en envases</option>
            </select></div>
        )}
        <div className="field"><label htmlFor="contenido">Contenido de cada {nombreEnvase(datos.unidadMedida, false)} ({unidad(datos.unidadMedida)})</label>
          <input id="contenido" type="number" min="1" step="1" required value={datos.contenidoEnvase} onChange={cambiar('contenidoEnvase')} /></div>
        <div className="field"><label htmlFor="costo">Costo por {unidad(datos.unidadMedida)} (ARS)</label>
          <input id="costo" type="number" min="0" step="0.01" required value={datos.costoUnitario} onChange={cambiar('costoUnitario')} />
          <small>Cada {nombreEnvase(datos.unidadMedida, false)} ≈ {pesos(costoEnvase)}</small></div>
        <div className="field"><label htmlFor="reorden">Punto de reposición ({unidad(datos.unidadMedida)})</label>
          <input id="reorden" type="number" min="0" step="1" required value={datos.puntoReorden} onChange={cambiar('puntoReorden')} />
          <small>Al llegar a esta cantidad se avisa para reponer.</small></div>
      </div>
      {nuevo && (
        <>
          <div className="small strong mt16 mb8">Stock inicial</div>
          <div className="fieldrow">
            <div className="field"><label htmlFor="envasesIniciales">{envasesCerrados(datos.unidadMedida)}</label>
              <input id="envasesIniciales" type="number" min="0" step="1" required value={datos.envasesIniciales} onChange={cambiar('envasesIniciales')} /></div>
            <div className="field"><label htmlFor="restanteInicial">Lo que queda en la abierta ({unidad(datos.unidadMedida)})</label>
              <input id="restanteInicial" type="number" min="0" max={contenido || undefined} step="1" required value={datos.restanteInicial}
                     onChange={cambiar('restanteInicial')} /></div>
          </div>
          <div className="note info mt12">
            Stock inicial: <b>{cantidad(stockInicial, datos.unidadMedida)}</b> ({enEnvases(stockInicial, contenido, datos.unidadMedida)}).
          </div>
        </>
      )}
      {!nuevo && <div className="note info mt12">El stock no se edita desde acá: usá las compras o el recuento para que quede registrado.</div>}
    </Modal>
  );
}
