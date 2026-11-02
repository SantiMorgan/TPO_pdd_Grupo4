import { api } from '../api/client.js';
import { useSesion } from '../session/Sesion.jsx';
import { hora } from './formato.js';
import { Modal } from './ui.jsx';
import { useDatos } from './useDatos.js';

export const PATRONES = [
  ['State', 'Comandas', 'Registrada, EnPreparacion, Despachada y Anulada deciden qué acciones admite cada comanda.', 'model/comanda/state'],
  ['Strategy', 'Precios', 'PrecioNormal, PrecioHappyHour y PrecioDemandaAlta se eligen en ejecución según la política del bar.', 'model/precio'],
  ['Observer', 'Stock', 'Inventario avisa cada cambio; AlertaStock reacciona al cruzar el punto de reposición.', 'model/inventario'],
  ['Simple Factory', 'Avisos', 'NotificacionFactory crea el canal (panel, email, SMS, WhatsApp) configurado por el bar.', 'model/notificacion'],
  ['Facade', 'Operaciones', 'BarStockFacade es la única entrada de los controladores y coordina cada caso de uso.', 'facade'],
  ['Decorator', 'Reportes', 'ReporteConGrafico, ConTendencias, ConMermas y ConMargen envuelven al ReporteBase.', 'model/reporte'],
];

/** Lo que registró el backend al ejecutar cada patrón, del más reciente al más antiguo. */
export function TrazaPatrones({ limite = 14, intervalo = 4000 }) {
  const { establecimientoId } = useSesion();
  const traza = useDatos(() => api.traza(establecimientoId), [establecimientoId], { intervalo });
  const eventos = traza.datos || [];
  return (
    <div className="trace" aria-live="polite">
      {eventos.length
        ? eventos.slice(0, limite).map((e, i) => (
          <div className="trace-row" key={`${e.fecha}-${i}`}>
            <time>{hora(e.fecha)}</time> <b>[{e.patron}]</b> {e.descripcion}
          </div>
        ))
        : 'Las operaciones aparecerán acá a medida que se registren, preparen y despachen comandas…'}
    </div>
  );
}

export function PatronesModal({ abierto, onCerrar }) {
  return (
    <Modal abierto={abierto} titulo="Seis patrones aplicados" onCerrar={onCerrar}>
      <div className="pattern-grid" style={{ gridTemplateColumns: 'repeat(2,1fr)' }}>
        {PATRONES.map(([id, modulo, descripcion, paquete]) => (
          <div className="pattern" key={id}>
            <div className="pattern-id">{id}</div>
            <h3>{modulo}</h3>
            <p>{descripcion}</p>
            <p className="mono tiny mt8">backend/…/{paquete}</p>
          </div>
        ))}
      </div>
      <div className="small strong mt16 mb8">Patrones en ejecución</div>
      {abierto && <TrazaPatrones limite={20} intervalo={2500} />}
      <div className="note info mt12">
        Además: MVC (vista React, controladores REST, modelo), Repository y DTO para el acceso a datos,
        y GRASP (Experto, Creador, Controlador, Fabricación pura, Variaciones protegidas).
      </div>
    </Modal>
  );
}

export function GuiaModal({ abierto, onCerrar }) {
  return (
    <Modal abierto={abierto} titulo="Guía para la presentación" onCerrar={onCerrar}>
      <div className="guide-highlight mb14">
        <div className="strong">Guion de 5 minutos · TPO Grupo 4</div>
        <div className="muted small">Abrí dos pestañas: una como Cliente y otra como Bartender del mismo bar.</div>
      </div>
      <ol className="small" style={{ paddingLeft: 22, lineHeight: 1.8 }}>
        <li><b>0:00–0:40 · Problema:</b> el stock de botellas abiertas no se descuenta solo.</li>
        <li><b>0:40–1:30 · Cliente:</b> elegir Bar Ámbar, pedir un Negroni (precio con la estrategia vigente).</li>
        <li><b>1:30–2:40 · Bartender:</b> el pedido aparece solo; preparar y despachar. El cliente ve el cambio de estado (State) y salta la alerta de Campari (Observer + Factory).</li>
        <li><b>2:40–3:30 · Encargado:</b> comprar un envase, cargar un recuento físico y ver la merma.</li>
        <li><b>3:30–4:30 · Dueño:</b> forzar Happy Hour (Strategy) y armar el reporte con decoradores (Decorator).</li>
        <li><b>4:30–5:00 · Cierre:</b> mostrar la traza de patrones y la Facade que coordinó todo.</li>
      </ol>
      <div className="note info mt12">
        Si falta stock al despachar, no se descuenta ningún insumo y la comanda sigue en preparación (transacción única).
      </div>
    </Modal>
  );
}
