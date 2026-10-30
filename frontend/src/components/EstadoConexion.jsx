export default function EstadoConexion({ conexion }) {
  const { cargando, estado, error } = conexion;
  return (
    <section className="connection" role="status" aria-live="polite">
      <strong>{cargando ? 'Comprobando conexión…' : error ? 'Sin conexión con la API' : 'Sistema conectado'}</strong>
      <span>{error || (estado ? `${estado.aplicacion} · Base de datos ${estado.salud}` : 'Verificando API y base de datos')}</span>
    </section>
  );
}
