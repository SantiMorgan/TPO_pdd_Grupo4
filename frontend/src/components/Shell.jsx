import { useState } from 'react';
import { api } from '../api/client.js';
import { ROLES } from '../routes/roles.js';
import { useSesion } from '../session/Sesion.jsx';
import EstadoConexion from './EstadoConexion.jsx';
import { GuiaModal, PatronesModal } from './Patrones.jsx';
import { Pill } from './ui.jsx';
import { useDatos } from './useDatos.js';

/** Barra lateral y superior del prototipo, con la navegación del rol que ingresó. */
export default function Shell({ vista, ir, children }) {
  const { sesion, establecimientos, establecimientoId, establecimiento, cambiarEstablecimiento, salir } = useSesion();
  const rol = ROLES[sesion.rol];
  const [modal, setModal] = useState(null);
  const carta = useDatos(() => (establecimientoId ? api.carta(establecimientoId) : null), [establecimientoId], { intervalo: 5000 });
  const estrategia = carta.datos?.estrategia;
  const titulo = rol.vistas.find((v) => v.id === vista)?.titulo || '';

  const selectorBar = (clase, etiqueta) => (
    <select className={clase} aria-label={etiqueta} value={establecimientoId || ''}
            onChange={(e) => cambiarEstablecimiento(e.target.value)}>
      {establecimientos.map((b) => <option key={b.id} value={b.id}>{b.nombre} · {b.zona}</option>)}
    </select>
  );

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">🍸</div>
          <div>
            <div className="brand-name">BarStock<span style={{ color: '#dfa372' }}>.</span></div>
            <div className="brand-sub">Solutions · Grupo 4</div>
          </div>
        </div>
        <div className="rol-actual">
          <span className="rol-icono" aria-hidden="true">{rol.icono}</span>
          <div>
            <b>{rol.nombre}</b>
            <span>{sesion.nombre || 'Sin nombre'}</span>
          </div>
          <button type="button" onClick={salir}>Cambiar rol</button>
        </div>
        <div>
          <div className="venue-label caps">Establecimiento</div>
          {selectorBar('venue-select', 'Establecimiento activo')}
        </div>
        <div>
          <div className="nav-title">{rol.nombre.toUpperCase()}</div>
          <nav className="nav" aria-label="Vistas del rol">
            {rol.vistas.map((v) => (
              <button key={v.id} type="button" className={vista === v.id ? 'active' : ''}
                      aria-current={vista === v.id ? 'page' : 'false'} onClick={() => ir(v.id)}>
                <span className="ico">{v.icono}</span>{v.titulo}
              </button>
            ))}
          </nav>
        </div>
        <div className="sidebar-footer">
          <EstadoConexion />
          <p className="tiny">UADE · Proceso de Desarrollo de Software</p>
          <p className="tiny">Spring Boot · PostgreSQL · React</p>
        </div>
      </aside>
      <div className="app-main">
        <header className="app-top">
          <div>
            <div className="top-path">
              BarStock Solutions <span style={{ margin: '0 5px' }}>/</span> {establecimiento?.nombre}
              <span style={{ margin: '0 5px' }}>/</span> <b>{titulo}</b>
            </div>
            {selectorBar('mobile-venue', 'Cambiar establecimiento')}
          </div>
          <div className="top-actions">
            {estrategia && (
              <span className="chip" title={estrategia.motivo}>
                Precio vigente <Pill tono={estrategia.codigo === 'NORMAL' ? undefined : estrategia.codigo === 'HAPPY_HOUR' ? 'success' : 'warn'}>{estrategia.nombre}</Pill>
              </span>
            )}
            <button className="btn" type="button" onClick={() => setModal('patrones')}>◈ Patrones</button>
            <button className="btn btn-ghost" type="button" onClick={() => setModal('guia')}>📋 Guía</button>
          </div>
        </header>
        <main id="contenido" aria-live="polite">{children}</main>
      </div>
      <PatronesModal abierto={modal === 'patrones'} onCerrar={() => setModal(null)} />
      <GuiaModal abierto={modal === 'guia'} onCerrar={() => setModal(null)} />
    </div>
  );
}
