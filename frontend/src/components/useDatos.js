import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * Carga datos de la API y, si se indica un intervalo, los vuelve a pedir periódicamente
 * (así el bartender ve los pedidos de los clientes y el cliente ve el estado de su comanda).
 */
export function useDatos(cargar, dependencias, { intervalo } = {}) {
  const [estado, setEstado] = useState({ datos: null, error: null, cargando: true });
  const cargarRef = useRef(cargar);
  cargarRef.current = cargar;

  const recargar = useCallback(async () => {
    try {
      const datos = await cargarRef.current();
      setEstado({ datos, error: null, cargando: false });
      return datos;
    } catch (error) {
      setEstado((anterior) => ({ ...anterior, error, cargando: false }));
      return null;
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, dependencias);

  useEffect(() => {
    setEstado((anterior) => ({ ...anterior, cargando: true }));
    recargar();
    if (!intervalo) return undefined;
    // En una pestaña oculta no se consulta; al volver a mostrarse se actualiza enseguida.
    const temporizador = setInterval(() => {
      if (document.visibilityState !== 'hidden') recargar();
    }, intervalo);
    const alMostrarse = () => {
      if (document.visibilityState === 'visible') recargar();
    };
    document.addEventListener('visibilitychange', alMostrarse);
    return () => {
      clearInterval(temporizador);
      document.removeEventListener('visibilitychange', alMostrarse);
    };
  }, [recargar, intervalo]);

  return { ...estado, recargar };
}
