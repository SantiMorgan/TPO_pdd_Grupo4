import { api } from '../../api/client.js';
import { HistorialAuditorias, RecuentoFisico } from '../../components/inventario.jsx';
import { Cargando, Encabezado, Tarjeta } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

export default function CierreTurnoView() {
  const { establecimientoId, sesion } = useSesion();
  const insumos = useDatos(() => api.insumos.listar(establecimientoId), [establecimientoId]);
  const auditorias = useDatos(() => api.auditorias.listar(establecimientoId), [establecimientoId]);
  const responsable = `Bartender${sesion.nombre ? ` · ${sesion.nombre}` : ''}`;

  return (
    <>
      <Encabezado titulo="Cierre de turno"
                  descripcion="Al terminar el turno, medí lo que queda en cada botella abierta. El sistema compara contra el stock teórico y registra la merma." />
      <div className="cols-2" style={{ gridTemplateColumns: '1.4fr .8fr' }}>
        <Cargando estado={insumos}>
          {(lista) => (
            <RecuentoFisico insumos={lista} responsable={responsable}
                            onRegistrado={() => { insumos.recargar(); auditorias.recargar(); }} />
          )}
        </Cargando>
        <Tarjeta titulo="Mis últimos cierres" subtitulo="Los audita el encargado">
          <HistorialAuditorias auditorias={(auditorias.datos || []).slice(0, 5)} />
        </Tarjeta>
      </div>
    </>
  );
}
