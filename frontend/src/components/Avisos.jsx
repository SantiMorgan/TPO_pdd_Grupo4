import { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';

const AvisosContext = createContext(() => {});

/** Toast del prototipo: confirma acciones y muestra los errores que devuelve la API. */
export function AvisosProvider({ children }) {
  const [aviso, setAviso] = useState(null);
  const temporizador = useRef(null);

  const avisar = useCallback((texto, esError = false) => {
    clearTimeout(temporizador.current);
    setAviso({ texto, esError });
    temporizador.current = setTimeout(() => setAviso(null), esError ? 9000 : 5000);
  }, []);

  useEffect(() => () => clearTimeout(temporizador.current), []);

  return (
    <AvisosContext.Provider value={avisar}>
      {children}
      {aviso && (
        <div className={`toast${aviso.esError ? ' error' : ''}`} role="status" aria-live="polite">
          <span>{aviso.esError ? '⚠' : '✓'} {aviso.texto}</span>
          <button type="button" onClick={() => setAviso(null)} aria-label="Cerrar">×</button>
        </div>
      )}
    </AvisosContext.Provider>
  );
}

export const useAvisos = () => useContext(AvisosContext);

/** Ejecuta una acción contra la API y avisa el resultado. Devuelve true si salió bien. */
export function useAccion() {
  const avisar = useAvisos();
  const [ocupado, setOcupado] = useState(false);
  const ejecutar = useCallback(async (accion, mensajeExito) => {
    setOcupado(true);
    try {
      const resultado = await accion();
      if (mensajeExito) avisar(typeof mensajeExito === 'function' ? mensajeExito(resultado) : mensajeExito);
      return resultado ?? true;
    } catch (error) {
      avisar(error.message, true);
      return false;
    } finally {
      setOcupado(false);
    }
  }, [avisar]);
  return [ejecutar, ocupado];
}
