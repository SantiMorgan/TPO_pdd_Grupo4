import { useEffect } from 'react';
import { AvisosProvider } from './components/Avisos.jsx';
import Shell from './components/Shell.jsx';
import VistaPorRol from './routes/VistaPorRol.jsx';
import { useRuta } from './routes/useRuta.js';
import { SesionProvider, useSesion } from './session/Sesion.jsx';
import Ingreso from './views/Ingreso.jsx';

function Aplicacion() {
  const { sesion, establecimientos, establecimiento, cambiarEstablecimiento } = useSesion();
  const [vista, ir] = useRuta();

  // Si el bar guardado en la sesión ya no existe, se usa el primero disponible.
  useEffect(() => {
    if (sesion && establecimientos.length && !establecimiento) cambiarEstablecimiento(establecimientos[0].id);
  }, [sesion, establecimientos, establecimiento, cambiarEstablecimiento]);

  if (!sesion) return <Ingreso />;
  return (
    <Shell vista={vista} ir={ir}>
      <VistaPorRol vista={vista} ir={ir} />
    </Shell>
  );
}

export default function App() {
  return (
    <AvisosProvider>
      <SesionProvider>
        <Aplicacion />
      </SesionProvider>
    </AvisosProvider>
  );
}
