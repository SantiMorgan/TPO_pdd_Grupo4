import { useEffect, useState } from 'react';
import { consultarEstado } from './api/client.js';
import EstadoConexion from './components/EstadoConexion.jsx';
import VistaPorRol from './routes/VistaPorRol.jsx';

export default function App() {
  const [rol, setRol] = useState('bartender');
  const [conexion, setConexion] = useState({ cargando: true });

  useEffect(() => {
    const controller = new AbortController();
    consultarEstado(controller.signal)
      .then((estado) => setConexion({ estado }))
      .catch((error) => {
        if (error.name !== 'AbortError') setConexion({ error: error.message });
      });
    return () => controller.abort();
  }, []);

  return (
    <main>
      <header>
        <p className="eyebrow">CONTROL DE INVENTARIO GASTRONÓMICO</p>
        <h1>BarStock <span>Solutions</span></h1>
        <p>La base para gestionar tu barra, desde el primer insumo hasta el último trago.</p>
      </header>
      <EstadoConexion conexion={conexion} />
      <nav aria-label="Seleccionar vista">
        <button aria-pressed={rol === 'bartender'} onClick={() => setRol('bartender')}>Bartender</button>
        <button aria-pressed={rol === 'encargado'} onClick={() => setRol('encargado')}>Encargado</button>
      </nav>
      <VistaPorRol rol={rol} />
      <footer>Hito 0 · Vistas iniciales. La selección de rol todavía no incluye autenticación.</footer>
    </main>
  );
}
