import { useEffect, useState } from 'react';
import { ROLES } from '../routes/roles.js';
import { useSesion } from '../session/Sesion.jsx';

/** Pantalla inicial: el usuario elige el bar y con qué rol entra. Sin contraseña (no hay autenticación). */
export default function Ingreso() {
  const { establecimientos, cargandoBares, errorBares, recargarEstablecimientos, ingresar } = useSesion();
  const [rol, setRol] = useState('CLIENTE');
  const [nombre, setNombre] = useState('');
  const [bar, setBar] = useState('');

  useEffect(() => {
    if (!bar && establecimientos.length) setBar(establecimientos[0].id);
  }, [bar, establecimientos]);

  const enviar = (e) => {
    e.preventDefault();
    if (bar) ingresar(rol, nombre, bar);
  };

  return (
    <div className="ingreso">
      <form className="ingreso-card" onSubmit={enviar}>
        <div className="ingreso-head">
          <div className="brand-icon" style={{ width: 52, height: 52, fontSize: 28 }}>🍸</div>
          <div>
            <div className="eyebrow mb0">Inventario inteligente para gastronomía</div>
            <h1>BarStock Solutions</h1>
          </div>
        </div>
        <p className="muted">Cada mililitro cuenta. Elegí el establecimiento y con qué rol querés entrar: cada rol tiene su propia vista.</p>

        {errorBares && !establecimientos.length && (
          <div className="note danger mt12">
            No se pudo leer la lista de bares: {errorBares.message}.{' '}
            <button type="button" className="btn btn-small" onClick={recargarEstablecimientos}>Reintentar</button>
          </div>
        )}

        <div className="rol-grid" role="radiogroup" aria-label="Rol">
          {Object.entries(ROLES).map(([clave, r]) => (
            <button key={clave} type="button" role="radio" aria-checked={rol === clave}
                    className={`rol${rol === clave ? ' activo' : ''}`} onClick={() => setRol(clave)}>
              <span className="rol-icono" aria-hidden="true">{r.icono}</span>
              <span className="rol-nombre">{r.nombre}</span>
              <span className="rol-desc">{r.descripcion}</span>
              <span className="rol-vistas">{r.vistas.map((v) => v.titulo).join(' · ')}</span>
            </button>
          ))}
        </div>

        <div className="fieldrow">
          <div className="field">
            <label htmlFor="bar">Establecimiento</label>
            <select id="bar" value={bar} onChange={(e) => setBar(e.target.value)} disabled={cargandoBares && !establecimientos.length}>
              {!establecimientos.length && <option value="">{cargandoBares ? 'Cargando…' : 'No hay establecimientos'}</option>}
              {establecimientos.map((b) => <option key={b.id} value={b.id}>{b.nombre} · {b.zona}</option>)}
            </select>
          </div>
          <div className="field">
            <label htmlFor="nombre">{ROLES[rol].pideNombre}</label>
            <input id="nombre" maxLength={40} value={nombre} onChange={(e) => setNombre(e.target.value)}
                   placeholder={rol === 'CLIENTE' ? 'Ej.: Mesa 4' : 'Ej.: Lucas'} />
          </div>
        </div>
        <div className="flex between wrap gap12 mt16">
          <span className="tiny muted">La separación por rol es de interfaz; el inicio de sesión real queda como trabajo futuro.</span>
          <button className="btn btn-primary" type="submit" disabled={!bar}>Ingresar como {ROLES[rol].nombre} →</button>
        </div>
      </form>
    </div>
  );
}
