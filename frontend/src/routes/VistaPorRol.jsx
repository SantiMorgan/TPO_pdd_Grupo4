import { useEffect } from 'react';
import { useSesion } from '../session/Sesion.jsx';
import { vistaInicial, vistaPermitida } from './roles.js';
import CartaView from '../views/cliente/CartaView.jsx';
import MisPedidosView from '../views/cliente/MisPedidosView.jsx';
import BarraView from '../views/bartender/BarraView.jsx';
import StockView from '../views/bartender/StockView.jsx';
import CierreTurnoView from '../views/bartender/CierreTurnoView.jsx';
import InventarioView from '../views/encargado/InventarioView.jsx';
import AuditoriaView from '../views/encargado/AuditoriaView.jsx';
import AlertasView from '../views/encargado/AlertasView.jsx';
import ResumenView from '../views/dueno/ResumenView.jsx';
import ReportesView from '../views/dueno/ReportesView.jsx';
import PreciosView from '../views/dueno/PreciosView.jsx';
import MenuView from '../views/dueno/MenuView.jsx';
import EstablecimientosView from '../views/dueno/EstablecimientosView.jsx';

const VISTAS = {
  carta: CartaView,
  pedidos: MisPedidosView,
  barra: BarraView,
  stock: StockView,
  cierre: CierreTurnoView,
  inventario: InventarioView,
  auditoria: AuditoriaView,
  alertas: AlertasView,
  resumen: ResumenView,
  reportes: ReportesView,
  precios: PreciosView,
  menu: MenuView,
  establecimientos: EstablecimientosView,
};

/** Guarda por rol: una vista que no le corresponde al rol redirige a su vista inicial. */
export default function VistaPorRol({ vista, ir }) {
  const { sesion, establecimientoId } = useSesion();
  const permitida = vistaPermitida(sesion.rol, vista);

  useEffect(() => {
    if (!permitida) ir(vistaInicial(sesion.rol));
  }, [permitida, sesion.rol, ir]);

  if (!permitida || !establecimientoId) return null;
  const Vista = VISTAS[vista];
  // La key reinicia el estado de la vista al cambiar de establecimiento.
  return <Vista key={establecimientoId} ir={ir} />;
}
