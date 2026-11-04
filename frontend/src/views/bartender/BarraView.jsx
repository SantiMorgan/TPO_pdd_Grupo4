import { useState } from 'react';
import { api } from '../../api/client.js';
import { useAccion } from '../../components/Avisos.jsx';
import { AnularModal, DevolucionModal, TarjetaComanda } from '../../components/comandas.jsx';
import { pesos } from '../../components/formato.js';
import { ListaNotificaciones } from '../../components/inventario.jsx';
import { Carrito, MenuTragos, usePedido } from '../../components/pedido.jsx';
import { Cargando, Encabezado, Pill, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

export default function BarraView() {
  const { establecimientoId, establecimiento } = useSesion();
  const carta = useDatos(() => api.carta(establecimientoId), [establecimientoId], { intervalo: 8000 });
  const enCurso = useDatos(() => api.comandas.enCurso(establecimientoId), [establecimientoId], { intervalo: 3000 });
  const recientes = useDatos(() => api.comandas.recientes(establecimientoId, 12), [establecimientoId], { intervalo: 6000 });
  const avisos = useDatos(() => api.notificaciones(establecimientoId, 4), [establecimientoId], { intervalo: 5000 });
  const pedido = usePedido();
  const [mesa, setMesa] = useState('');
  const [anulando, setAnulando] = useState(null);
  const [devolviendo, setDevolviendo] = useState(null);
  const [ejecutar, ocupado] = useAccion();

  const refrescar = () => {
    enCurso.recargar();
    recientes.recargar();
    carta.recargar();
    setTimeout(avisos.recargar, 300);
  };

  const registrar = async () => {
    const comanda = await ejecutar(
      () => api.comandas.registrar(establecimientoId, { origen: 'BARTENDER', cliente: mesa, items: pedido.items }),
      (c) => `Comanda #${c.numero} registrada; iniciá la preparación`,
    );
    if (comanda) {
      pedido.vaciar();
      setMesa('');
    }
    refrescar();
  };

  const preparar = async (c) => {
    await ejecutar(() => api.comandas.preparar(establecimientoId, c.id), `Comanda #${c.numero} en preparación`);
    refrescar();
  };

  const despachar = async (c) => {
    await ejecutar(() => api.comandas.despachar(establecimientoId, c.id),
      (d) => `Comanda #${d.numero} despachada por ${pesos(d.total)}; stock descontado`);
    refrescar();
  };

  const anular = async (c, motivo) => {
    if (await ejecutar(() => api.comandas.anular(establecimientoId, c.id, motivo), `Comanda #${c.numero} anulada`)) {
      setAnulando(null);
    }
    refrescar();
  };

  const devolver = async (c, devolucion) => {
    if (await ejecutar(() => api.comandas.devolver(establecimientoId, c.id, devolucion),
      (d) => `Devolución registrada en la comanda #${d.numero}: queda cobrado ${pesos(d.totalNeto)}`)) {
      setDevolviendo(null);
    }
    refrescar();
  };

  const despachadas = (recientes.datos || []).filter((c) => c.estado === 'DESPACHADA').slice(0, 5);
  const listaEnCurso = enCurso.datos || [];
  const deClientes = listaEnCurso.filter((c) => c.origen === 'CLIENTE' && c.estado === 'REGISTRADA').length;

  return (
    <>
      <Encabezado titulo="Barra y comandas"
                  descripcion="Tomá comandas, preparalas y despachalas. Los pedidos de los clientes llegan solos. El stock se descuenta únicamente al despachar."
                  extra={deClientes > 0 && <Pill tono="warn">🙋 {deClientes} pedido(s) nuevo(s) de clientes</Pill>} />
      <div className="cols-2" style={{ gridTemplateColumns: '1.25fr .87fr' }}>
        <div>
          <Tarjeta titulo="Comandas en curso" subtitulo="Cada comanda muestra solo lo que se puede hacer en su estado actual."
                   extra={<Pill>{listaEnCurso.length} en curso</Pill>}>
            <Cargando estado={enCurso}>
              {(lista) => (lista.length ? (
                <div className="order-list">
                  {lista.map((c) => (
                    <TarjetaComanda key={c.id} comanda={c} ocupado={ocupado} onPreparar={preparar} onDespachar={despachar} onAnular={setAnulando} />
                  ))}
                </div>
              ) : <Vacio simbolo="🧾">No hay comandas en curso. Registrá una a la derecha o esperá pedidos de clientes.</Vacio>)}
            </Cargando>
          </Tarjeta>
          <Tarjeta titulo={`Menú de ${establecimiento.nombre}`} subtitulo="Tocá un trago para sumarlo a la comanda nueva."
                   extra={carta.datos && <Pill tono="info">{carta.datos.estrategia.nombre}</Pill>}>
            <Cargando estado={carta}>{(datos) => <MenuTragos items={datos.items} onAgregar={pedido.agregar} mostrarCosto />}</Cargando>
          </Tarjeta>
          <Tarjeta titulo="Últimas despachadas" subtitulo="Si un cliente devuelve un trago, registralo acá: el resto del pedido sigue cobrado.">
            {despachadas.length ? (
              <div className="order-list">
                {despachadas.map((c) => <TarjetaComanda key={c.id} comanda={c} ocupado={ocupado} onDevolver={setDevolviendo} />)}
              </div>
            ) : <Vacio>Todavía no se despacharon comandas.</Vacio>}
          </Tarjeta>
        </div>
        <div>
          <Carrito titulo="Comanda nueva" subtitulo="Pedido tomado en la barra." carta={carta.datos} pedido={pedido}
                   onEnviar={registrar} ocupado={ocupado} textoEnviar="Registrar comanda →"
                   nota="El precio queda fijo al registrar la comanda. El stock no se reserva: se descuenta al despachar.">
            <div className="field mt12">
              <label htmlFor="mesa">Mesa o cliente (opcional)</label>
              <input id="mesa" maxLength={60} value={mesa} onChange={(e) => setMesa(e.target.value)} placeholder="Ej.: Barra 2" />
            </div>
          </Carrito>
          <Tarjeta titulo="Alertas de reposición" subtitulo="Insumos que llegaron a su punto de reposición" className="mt16"
                   extra={<Pill tono={avisos.datos?.length ? 'warn' : undefined}>{avisos.datos?.length ?? 0}</Pill>}>
            <ListaNotificaciones notificaciones={avisos.datos || []} limite={4} />
          </Tarjeta>
        </div>
      </div>
      <AnularModal key={anulando?.id} comanda={anulando} ocupado={ocupado} onCerrar={() => setAnulando(null)} onConfirmar={anular} />
      <DevolucionModal key={devolviendo ? `d-${devolviendo.id}` : 'sin-devolucion'} comanda={devolviendo} ocupado={ocupado}
                       onCerrar={() => setDevolviendo(null)} onConfirmar={devolver} />
    </>
  );
}
