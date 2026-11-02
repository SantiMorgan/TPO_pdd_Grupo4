import { api } from '../../api/client.js';
import { fechaHora } from '../../components/formato.js';
import { AlertasActivas } from '../../components/inventario.jsx';
import { Cargando, Encabezado, Pill, Tarjeta, Vacio } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

const ICONOS = { PANEL: '🖥', EMAIL: '✉', SMS: '💬', WHATSAPP: '🟢' };

export default function AlertasView() {
  const { establecimientoId, establecimiento } = useSesion();
  const avisos = useDatos(() => api.notificaciones(establecimientoId, 50), [establecimientoId], { intervalo: 5000 });
  const insumos = useDatos(() => api.insumos.listar(establecimientoId), [establecimientoId], { intervalo: 10000 });
  const { canales, email, telefono } = establecimiento.alertas;

  return (
    <>
      <Encabezado titulo="Alertas de reposición"
                  descripcion="Cuando un insumo cruza su punto de reposición, AlertaStock (Observer) pide a NotificacionFactory un canal por cada medio configurado." />
      <div className="cols-2">
        <Tarjeta titulo="Avisos emitidos" subtitulo="Un registro por canal; email, SMS y WhatsApp están simulados"
                 extra={<Pill tono={avisos.datos?.length ? 'warn' : undefined}>{avisos.datos?.length ?? 0}</Pill>}>
          <Cargando estado={avisos}>
            {(lista) => (lista.length ? (
              <div className="table-wrap">
                <table>
                  <thead><tr><th>Fecha</th><th>Canal</th><th>Destinatario</th><th>Mensaje</th><th>Resultado</th></tr></thead>
                  <tbody>
                    {lista.map((n) => (
                      <tr key={n.id}>
                        <td>{fechaHora(n.fecha)}</td>
                        <td>{ICONOS[n.canal]} {n.canal}</td>
                        <td>{n.destinatario}</td>
                        <td style={{ whiteSpace: 'normal', minWidth: 220 }}>{n.mensaje}</td>
                        <td><Pill tono={n.resultado === 'FALLIDA' ? 'danger' : n.resultado === 'SIMULADA' ? 'info' : 'success'}>{n.resultado}</Pill></td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : <Vacio simbolo="🔔">Todavía no se dispararon alertas en este establecimiento.</Vacio>)}
          </Cargando>
        </Tarjeta>
        <div>
          <Tarjeta titulo="Canales configurados" subtitulo="Los define el dueño en Establecimientos">
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
          <Tarjeta titulo="Cómo funciona">
            <ul className="small-list">
              <li><span>1</span>Cada despacho, compra o ajuste confirma su transacción y recién después el Inventario avisa a sus observadores.</li>
              <li><span>2</span>AlertaStock solo actúa cuando el stock cruza hacia abajo el punto de reposición: no repite el aviso con cada trago.</li>
              <li><span>3</span>NotificacionFactory crea el canal concreto; si uno falla, el descuento ya confirmado no se deshace.</li>
            </ul>
          </Tarjeta>
        </div>
      </div>
    </>
  );
}
