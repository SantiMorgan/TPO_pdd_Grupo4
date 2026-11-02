import { useState } from 'react';
import { api } from '../../api/client.js';
import { useAccion } from '../../components/Avisos.jsx';
import { horaCorta } from '../../components/formato.js';
import { Carrito, MenuTragos, usePedido } from '../../components/pedido.jsx';
import { Cargando, Encabezado, Pill, Tarjeta } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';
import { recordarPedido } from './misPedidos.js';

export default function CartaView({ ir }) {
  const { establecimientoId, establecimiento, sesion } = useSesion();
  const carta = useDatos(() => api.carta(establecimientoId), [establecimientoId], { intervalo: 10000 });
  const pedido = usePedido();
  const [cliente, setCliente] = useState(sesion.nombre || '');
  const [ejecutar, ocupado] = useAccion();
  const estrategia = carta.datos?.estrategia;

  const enviar = async () => {
    const comanda = await ejecutar(
      () => api.comandas.registrar(establecimientoId, { origen: 'CLIENTE', cliente, items: pedido.items }),
      (c) => `Pedido #${c.numero} enviado a la barra`,
    );
    if (comanda) {
      recordarPedido(establecimientoId, comanda.id);
      pedido.vaciar();
      ir('pedidos');
    } else {
      carta.recargar();
    }
  };

  return (
    <>
      <Encabezado
        titulo={`Carta de ${establecimiento.nombre}`}
        descripcion="Elegí tus tragos y mandá el pedido a la barra. El precio que ves es el de este momento: puede cambiar con el Happy Hour o la demanda."
        extra={estrategia && (
          <div className="flex gap8 wrap">
            <span className="chip">🕐 {horaCorta(estrategia.hora)}{estrategia.horaSimulada ? ' (simulada)' : ''}</span>
            <Pill tono={estrategia.codigo === 'HAPPY_HOUR' ? 'success' : estrategia.codigo === 'DEMANDA_ALTA' ? 'warn' : 'info'}>{estrategia.nombre}</Pill>
          </div>
        )}
      />
      {estrategia?.codigo === 'HAPPY_HOUR' && (
        <div className="note success mb14">🎉 ¡Es Happy Hour! {estrategia.motivo}. Los precios ya tienen el descuento aplicado.</div>
      )}
      {estrategia?.codigo === 'DEMANDA_ALTA' && (
        <div className="note mb14">La barra está con mucha demanda: rige un recargo temporal ({estrategia.nombre}).</div>
      )}
      <div className="cols-2" style={{ gridTemplateColumns: '1.25fr .87fr' }}>
        <Tarjeta titulo="Tragos" subtitulo={establecimiento.descripcion}
                 extra={<Pill>{carta.datos?.items.length ?? 0} tragos</Pill>}>
          <Cargando estado={carta}>
            {(datos) => <MenuTragos items={datos.items} onAgregar={pedido.agregar} />}
          </Cargando>
        </Tarjeta>
        <div>
          <Carrito titulo="Tu pedido" subtitulo="Podés sumar varios tragos y unidades." carta={carta.datos} pedido={pedido}
                   onEnviar={enviar} ocupado={ocupado} textoEnviar="Enviar pedido a la barra →"
                   nota="El precio se fija cuando se registra el pedido. El stock se descuenta recién cuando el bartender lo despacha.">
            <div className="field mt12">
              <label htmlFor="cliente">Nombre o mesa</label>
              <input id="cliente" maxLength={60} value={cliente} onChange={(e) => setCliente(e.target.value)} placeholder="Ej.: Mesa 4" />
            </div>
          </Carrito>
          <Tarjeta titulo="¿Cómo sigue?" className="mt16">
            {[['1', 'Registrada', 'La barra recibe tu pedido.'], ['2', 'En preparación', 'El bartender lo está preparando.'], ['3', 'Despachada', 'Tu trago está listo.']].map(([n, t, d]) => (
              <div className="step" key={n}><div className="step-n">{n}</div><div><h4>{t}</h4><p>{d}</p></div></div>
            ))}
            <button className="btn mt12" type="button" onClick={() => ir('pedidos')}>Ver mis pedidos →</button>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
