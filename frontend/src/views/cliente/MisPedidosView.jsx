import { Fragment, useState } from 'react';
import { api } from '../../api/client.js';
import { useAccion } from '../../components/Avisos.jsx';
import { itemsTexto, PillEstado } from '../../components/comandas.jsx';
import { hora, pesos } from '../../components/formato.js';
import { Encabezado, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';
import { guardarMisPedidos, leerMisPedidos } from './misPedidos.js';

const PASOS = [
  ['REGISTRADA', 'Registrada', 'registradaEn'],
  ['EN_PREPARACION', 'En preparación', 'preparadaEn'],
  ['DESPACHADA', 'Despachada', 'despachadaEn'],
];

function Progreso({ comanda }) {
  const actual = PASOS.findIndex(([codigo]) => codigo === comanda.estado);
  return (
    <div className="stepper" aria-label={`Estado: ${comanda.estadoNombre}`}>
      {PASOS.map(([codigo, nombre, fecha], i) => {
        const hecho = comanda[fecha] && (i < actual || codigo === 'DESPACHADA');
        const clase = hecho ? 'hecho' : i === actual ? 'actual' : '';
        return (
          <Fragment key={codigo}>
            {i > 0 && <div className={`stepper-linea${comanda[fecha] ? ' hecho' : ''}`} />}
            <div className={`stepper-paso ${clase}`}>
              <span className="punto">{hecho ? '✓' : i + 1}</span>
              <span>{nombre}{comanda[fecha] ? ` · ${hora(comanda[fecha])}` : ''}</span>
            </div>
          </Fragment>
        );
      })}
    </div>
  );
}

export default function MisPedidosView({ ir }) {
  const { establecimientoId } = useSesion();
  const [ids, setIds] = useState(() => leerMisPedidos(establecimientoId));
  const [ejecutar, ocupado] = useAccion();

  // Cada pedido se consulta por su id; uno que ya no existe simplemente se omite.
  const pedidos = useDatos(async () => {
    const resultados = await Promise.all(ids.map((id) => api.comandas.consultar(establecimientoId, id).catch(() => null)));
    return resultados.filter(Boolean);
  }, [establecimientoId, ids], { intervalo: 3000 });
  const lista = pedidos.datos || [];

  const cancelar = async (comanda) => {
    if (await ejecutar(() => api.comandas.anular(establecimientoId, comanda.id, 'Cancelado por el cliente'),
      `Pedido #${comanda.numero} cancelado`)) pedidos.recargar();
  };

  const olvidarFinalizados = () => {
    const activos = lista.filter((c) => c.estado === 'REGISTRADA' || c.estado === 'EN_PREPARACION').map((c) => c.id);
    guardarMisPedidos(establecimientoId, activos);
    setIds(activos);
  };

  return (
    <>
      <Encabezado titulo="Mis pedidos" descripcion="Seguí en vivo cada pedido: el estado lo cambia la barra y se actualiza solo."
                  extra={<div className="flex gap8">
                    {lista.some((c) => c.estado === 'DESPACHADA' || c.estado === 'ANULADA') && (
                      <button className="btn btn-ghost" type="button" onClick={olvidarFinalizados}>Ocultar finalizados</button>
                    )}
                    <button className="btn btn-primary" type="button" onClick={() => ir('carta')}>+ Pedir algo más</button>
                  </div>} />
      {!ids.length || (!lista.length && !pedidos.cargando) ? (
        <Tarjeta>
          <Vacio simbolo="🧾">Todavía no hiciste pedidos desde este dispositivo.</Vacio>
          <button className="btn btn-primary mt16" type="button" onClick={() => ir('carta')}>Ver la carta →</button>
        </Tarjeta>
      ) : (
        <div className="order-list">
          {lista.map((c) => (
            <Tarjeta key={c.id} titulo={`Pedido #${c.numero}`}
                     subtitulo={`${c.cliente ? `${c.cliente} · ` : ''}enviado a las ${hora(c.registradaEn)}`}
                     extra={<PillEstado comanda={c} />}>
              <div className="order-items" style={{ fontSize: 13 }}>{itemsTexto(c)}</div>
              <div className="small"><b>{pesos(c.total)}</b> <span className="muted">· {c.estrategiaPrecio}</span></div>
              {c.estado === 'ANULADA' ? (
                <div className="note danger mt12">
                  {c.estadoAlAnular === 'DESPACHADA' ? 'El pedido se registró como devuelto.' : 'El pedido fue anulado.'} Motivo: {c.motivoAnulacion}
                </div>
              ) : (
                <Progreso comanda={c} />
              )}
              {c.estado === 'DESPACHADA' && <div className="note success mt12">🍸 ¡Tu pedido está listo!</div>}
              {c.estado === 'REGISTRADA' && c.acciones.includes('ANULAR') && (
                <button className="btn btn-small btn-danger mt12" type="button" disabled={ocupado} onClick={() => cancelar(c)}>
                  × Cancelar pedido
                </button>
              )}
            </Tarjeta>
          ))}
        </div>
      )}
    </>
  );
}
