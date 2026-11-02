import { api } from '../../api/client.js';
import { fechaHora } from '../../components/formato.js';
import { AlertasActivas } from '../../components/inventario.jsx';
import { Cargando, Encabezado, Pill, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

const ICONOS = { PANEL: '🖥', EMAIL: '✉', SMS: '💬', WHATSAPP: '🟢' };
const RESULTADOS = { MOSTRADA: ['En el panel', 'success'], SIMULADA: ['Envío de prueba', 'info'], FALLIDA: ['No se pudo enviar', 'danger'] };

const COMO_FUNCIONA = [
  'Cada insumo tiene un punto de reposición, por ejemplo 250 ml de Campari. Cuando el stock llega a ese número o baja de él, recibís una alerta.',
  'El stock baja cuando el bartender despacha un trago y cambia cuando se registra un recuento. Las compras lo suben.',
  'El aviso llega una sola vez, en el momento en que el insumo cruza el punto de reposición: no se repite con cada trago. Si después se repone y vuelve a bajar, avisa de nuevo.',
  'Llega por cada medio que eligió el dueño en Establecimientos: este panel, email, SMS o WhatsApp. Por ahora el email, el SMS y el WhatsApp son de prueba: quedan anotados acá pero no salen de verdad.',
  'Si un aviso no se puede enviar, el trago igual queda despachado y el stock descontado.',
];

export default function AlertasView() {
  const { establecimientoId, establecimiento } = useSesion();
  const avisos = useDatos(() => api.notificaciones(establecimientoId, 50), [establecimientoId], { intervalo: 5000 });
  const insumos = useDatos(() => api.insumos.listar(establecimientoId), [establecimientoId], { intervalo: 10000 });
  const { canales, email, telefono } = establecimiento.alertas;

  return (
    <>
      <Encabezado titulo="Alertas de reposición"
                  descripcion="Te avisamos cuando un insumo se está por terminar, para que lo compres a tiempo." />
      <div className="cols-2">
        <Tarjeta titulo="Avisos emitidos" subtitulo="Un registro por cada medio de aviso"
                 extra={<Pill tono={avisos.datos?.length ? 'warn' : undefined}>{avisos.datos?.length ?? 0}</Pill>}>
          <Cargando estado={avisos}>
            {(lista) => (lista.length ? (
              <div className="table-wrap">
                <table>
                  <thead><tr><th>Fecha</th><th>Medio</th><th>Destinatario</th><th>Mensaje</th><th>Resultado</th></tr></thead>
                  <tbody>
                    {lista.map((n) => {
                      const [texto, tono] = RESULTADOS[n.resultado] || [n.resultado];
                      return (
                        <tr key={n.id}>
                          <td>{fechaHora(n.fecha)}</td>
                          <td>{ICONOS[n.canal]} {n.canal}</td>
                          <td>{n.destinatario}</td>
                          <td style={{ whiteSpace: 'normal', minWidth: 220 }}>{n.mensaje}</td>
                          <td><Pill tono={tono}>{texto}</Pill></td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            ) : <Vacio simbolo="🔔">Todavía no se dispararon alertas en este establecimiento.</Vacio>)}
          </Cargando>
        </Tarjeta>
        <div>
          <Tarjeta titulo="Por dónde te avisamos" subtitulo="Lo elige el dueño en Establecimientos">
            {canales.map((c) => (
              <div className="record" key={c}>
                <div className="record-label">{ICONOS[c]} {c}</div>
                <div className="record-sub">{c === 'PANEL' ? 'Panel de la aplicación' : c === 'EMAIL' ? email : telefono}</div>
              </div>
            ))}
          </Tarjeta>
          <Tarjeta titulo="Insumos a reponer ahora">
            <Cargando estado={insumos}>{(lista) => <AlertasActivas insumos={lista} />}</Cargando>
          </Tarjeta>
          <Tarjeta titulo="Cómo funciona" subtitulo="Qué alertas recibís y cuándo">
            <ul className="small-list">
              {COMO_FUNCIONA.map((texto, i) => <li key={i}><span>{i + 1}</span>{texto}</li>)}
            </ul>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
