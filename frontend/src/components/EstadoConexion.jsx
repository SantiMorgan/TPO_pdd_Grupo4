import { api } from '../api/client.js';
import { useDatos } from './useDatos.js';

export default function EstadoConexion() {
  const estado = useDatos(() => api.estado(), [], { intervalo: 30000 });
  const conectado = estado.datos && !estado.error;
  return (
    <p role="status" aria-live="polite">
      <span className="status-dot" style={conectado ? undefined : { background: '#e0716f' }} />
      {estado.cargando && !estado.datos ? 'Comprobando conexión…' : conectado ? `API y base de datos ${estado.datos.salud}` : 'Sin conexión con la API'}
    </p>
  );
}
