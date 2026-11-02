import { createContext, useCallback, useContext, useMemo, useState } from 'react';
import { api } from '../api/client.js';
import { useDatos } from '../components/useDatos.js';

const CLAVE = 'barstock_sesion_v1';
const SesionContext = createContext(null);

// sessionStorage: cada pestaña puede tener su propio rol (por ejemplo, Cliente y Bartender lado a lado).
function leer() {
  try {
    return JSON.parse(sessionStorage.getItem(CLAVE)) || null;
  } catch {
    return null;
  }
}

function guardar(sesion) {
  try {
    if (sesion) sessionStorage.setItem(CLAVE, JSON.stringify(sesion));
    else sessionStorage.removeItem(CLAVE);
  } catch {
    // Sin almacenamiento la sesión dura hasta recargar la página.
  }
}

/** Rol, nombre y establecimiento elegidos al ingresar, más la lista de bares. */
export function SesionProvider({ children }) {
  const [sesion, setSesion] = useState(leer);
  const bares = useDatos(() => api.establecimientos.listar(), []);

  const actualizar = useCallback((nueva) => {
    setSesion(nueva);
    guardar(nueva);
  }, []);

  const valor = useMemo(() => {
    const lista = bares.datos || [];
    const establecimiento = lista.find((b) => b.id === sesion?.establecimientoId) || null;
    return {
      sesion,
      establecimientos: lista,
      establecimiento,
      establecimientoId: establecimiento?.id,
      cargandoBares: bares.cargando,
      errorBares: bares.error,
      recargarEstablecimientos: bares.recargar,
      ingresar: (rol, nombre, establecimientoId) => actualizar({ rol, nombre: nombre.trim(), establecimientoId }),
      salir: () => actualizar(null),
      cambiarEstablecimiento: (establecimientoId) => actualizar({ ...sesion, establecimientoId }),
    };
  }, [sesion, bares.datos, bares.cargando, bares.error, bares.recargar, actualizar]);

  return <SesionContext.Provider value={valor}>{children}</SesionContext.Provider>;
}

export const useSesion = () => useContext(SesionContext);
