import { api } from '../../api/client.js';
import { AlertasActivas, KpisStock, TablaStock } from '../../components/inventario.jsx';
import { Cargando, Encabezado, Pill, Tarjeta } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

/** Consulta de stock en tiempo real, de solo lectura: las compras y ajustes son del encargado. */
export default function StockView({ ir }) {
  const { establecimientoId } = useSesion();
  const insumos = useDatos(() => api.insumos.listar(establecimientoId), [establecimientoId], { intervalo: 5000 });
  return (
    <>
      <Encabezado titulo="Stock disponible" descripcion="Existencias en mililitros o unidades, actualizadas con cada despacho."
                  extra={<button className="btn" type="button" onClick={() => ir('cierre')}>◈ Ir al cierre de turno</button>} />
      <Cargando estado={insumos}>
        {(lista) => (
          <>
            <KpisStock insumos={lista} />
            <div className="cols-2">
              <Tarjeta titulo="Insumos" subtitulo="Stock fraccionado por receta" extra={<Pill tono="info">Solo lectura</Pill>}>
                <TablaStock insumos={lista} />
              </Tarjeta>
              <Tarjeta titulo="Para avisar al encargado" subtitulo="Insumos en o por debajo de su punto de reposición">
                <AlertasActivas insumos={lista} />
              </Tarjeta>
            </div>
          </>
        )}
      </Cargando>
    </>
  );
}
