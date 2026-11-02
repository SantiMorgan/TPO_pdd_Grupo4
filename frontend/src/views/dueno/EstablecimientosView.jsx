import { useState } from 'react';
import { api } from '../../api/client.js';
import { useAccion } from '../../components/Avisos.jsx';
import { Encabezado, Modal, Pill, Tarjeta } from '../../components/ui.jsx';
import { useSesion } from '../../session/Sesion.jsx';

const CANALES = [['PANEL', '🖥 Panel de la aplicación'], ['EMAIL', '✉ Email (simulado)'], ['SMS', '💬 SMS (simulado)'], ['WHATSAPP', '🟢 WhatsApp (simulado)']];

function DatosModal({ abierto, establecimiento, onCerrar, onGuardar, ocupado }) {
  const nuevo = !establecimiento;
  const [datos, setDatos] = useState(() => ({
    codigo: '', nombre: establecimiento?.nombre ?? '', zona: establecimiento?.zona ?? '', descripcion: establecimiento?.descripcion ?? 'Bar de coctelería',
  }));
  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));
  return (
    <Modal abierto={abierto} titulo={nuevo ? 'Agregar establecimiento' : 'Editar establecimiento'} onCerrar={onCerrar}
           ocupado={ocupado} onEnviar={() => onGuardar(datos)}>
      <div className="fieldrow">
        {nuevo && <div className="field"><label htmlFor="ecodigo">Código</label>
          <input id="ecodigo" required maxLength={30} value={datos.codigo} onChange={cambiar('codigo')} placeholder="Ej.: CENTRO" /></div>}
        <div className="field"><label htmlFor="enombre">Nombre</label>
          <input id="enombre" required maxLength={80} value={datos.nombre} onChange={cambiar('nombre')} /></div>
        <div className="field"><label htmlFor="ezona">Barrio / ubicación</label>
          <input id="ezona" required maxLength={80} value={datos.zona} onChange={cambiar('zona')} /></div>
        <div className="field"><label htmlFor="edesc">Descripción</label>
          <input id="edesc" maxLength={150} value={datos.descripcion} onChange={cambiar('descripcion')} /></div>
      </div>
      {nuevo && <p className="muted small mt12">El bar nuevo empieza vacío: el encargado carga sus insumos y vos su menú.</p>}
    </Modal>
  );
}

function FormularioAlertas({ establecimiento, onGuardar, ocupado }) {
  const [canales, setCanales] = useState(establecimiento.alertas.canales);
  const [email, setEmail] = useState(establecimiento.alertas.email || '');
  const [telefono, setTelefono] = useState(establecimiento.alertas.telefono || '');
  const alternar = (canal) => setCanales((c) => (c.includes(canal) ? c.filter((x) => x !== canal) : [...c, canal]));
  return (
    <form onSubmit={(e) => { e.preventDefault(); onGuardar({ canales, email, telefono }); }}>
      <div className="casillas">
        {CANALES.map(([codigo, nombre]) => (
          <label key={codigo} className={`casilla${canales.includes(codigo) ? ' activa' : ''}`}>
            <input type="checkbox" checked={canales.includes(codigo)} onChange={() => alternar(codigo)} /> {nombre}
          </label>
        ))}
      </div>
      <div className="fieldrow mt12">
        <div className="field"><label htmlFor="aemail">Email del encargado</label>
          <input id="aemail" type="email" maxLength={120} value={email} onChange={(e) => setEmail(e.target.value)} /></div>
        <div className="field"><label htmlFor="atel">Teléfono para SMS / WhatsApp</label>
          <input id="atel" maxLength={30} value={telefono} onChange={(e) => setTelefono(e.target.value)} /></div>
      </div>
      <div className="actions-row mt16">
        <button className="btn btn-primary" type="submit" disabled={ocupado || !canales.length}>Guardar canales</button>
      </div>
    </form>
  );
}

export default function EstablecimientosView() {
  const { establecimientos, establecimiento, establecimientoId, cambiarEstablecimiento, recargarEstablecimientos } = useSesion();
  const [modal, setModal] = useState(null);
  const [ejecutar, ocupado] = useAccion();

  const guardarDatos = async (datos) => {
    const nuevo = modal === 'nuevo';
    const resultado = await ejecutar(() => (nuevo
      ? api.establecimientos.crear(datos)
      : api.establecimientos.editar(establecimientoId, datos)), 'Establecimiento guardado');
    if (resultado) {
      setModal(null);
      await recargarEstablecimientos();
      if (nuevo) cambiarEstablecimiento(resultado.id);
    }
  };

  const guardarAlertas = async (alertas) => {
    if (await ejecutar(() => api.establecimientos.configurarAlertas(establecimientoId, alertas), 'Canales de alerta guardados')) {
      recargarEstablecimientos();
    }
  };

  return (
    <>
      <Encabezado titulo="Establecimientos"
                  descripcion="Cada bar mantiene inventario, menú, comandas, precios y alertas independientes."
                  extra={<button className="btn btn-primary" type="button" onClick={() => setModal('nuevo')}>+ Agregar establecimiento</button>} />
      <div className="cols-half">
        <Tarjeta titulo="Establecimiento activo" subtitulo="Identidad del local" extra={<Pill tono="info">{establecimientos.length} bares</Pill>}>
          <div className="flex gap12" style={{ alignItems: 'flex-start' }}>
            <div className="brand-icon" style={{ width: 53, height: 53, flexShrink: 0 }}>🍸</div>
            <div>
              <h2>{establecimiento.nombre}</h2>
              <p className="muted small">{establecimiento.codigo} · {establecimiento.zona} · {establecimiento.descripcion}</p>
              <button className="btn btn-small" type="button" onClick={() => setModal('editar')}>Editar datos</button>
            </div>
          </div>
          <div className="divider" />
          <div className="small strong mb8">Tus bares</div>
          {establecimientos.map((b) => (
            <div className="record" key={b.id}>
              <div><div className="record-label">{b.nombre}</div><div className="record-sub">{b.zona} · precios {b.politicaPrecio.modo.toLowerCase()}</div></div>
              <button className={`btn btn-small${b.id === establecimientoId ? ' btn-green' : ''}`} type="button"
                      onClick={() => cambiarEstablecimiento(b.id)}>{b.id === establecimientoId ? 'Activo' : 'Cambiar'}</button>
            </div>
          ))}
          <div className="note info mt16">La separación es por establecimiento dentro de la aplicación; no hay usuarios ni permisos por bar todavía.</div>
        </Tarjeta>
        <Tarjeta titulo="Canales de alerta" subtitulo="Qué notificaciones crea NotificacionFactory cuando un insumo llega al punto de reposición">
          <FormularioAlertas key={establecimientoId} establecimiento={establecimiento} onGuardar={guardarAlertas} ocupado={ocupado} />
        </Tarjeta>
      </div>
      <DatosModal key={modal || 'cerrado'} abierto={Boolean(modal)} establecimiento={modal === 'editar' ? establecimiento : null}
                  ocupado={ocupado} onCerrar={() => setModal(null)} onGuardar={guardarDatos} />
    </>
  );
}
