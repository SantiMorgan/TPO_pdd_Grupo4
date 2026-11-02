import { useCallback, useEffect, useState } from 'react';

const leer = () => window.location.hash.replace(/^#\/?/, '').split('?')[0];

/** Ruteo mínimo por hash (#/barra): alcanza para la SPA y no agrega dependencias. */
export function useRuta() {
  const [ruta, setRuta] = useState(leer);
  useEffect(() => {
    const alCambiar = () => setRuta(leer());
    window.addEventListener('hashchange', alCambiar);
    return () => window.removeEventListener('hashchange', alCambiar);
  }, []);
  const ir = useCallback((vista) => {
    if (leer() !== vista) window.location.hash = `/${vista}`;
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }, []);
  return [ruta, ir];
}
