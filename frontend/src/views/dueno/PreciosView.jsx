import { useState } from 'react';
import { api } from '../../api/client.js';
import { useAccion } from '../../components/Avisos.jsx';
import { horaCorta, pesos } from '../../components/formato.js';
import { Cargando, Encabezado, Pill, Tarjeta } from '../../components/ui.jsx';
import { useDatos } from '../../components/useDatos.js';
import { useSesion } from '../../session/Sesion.jsx';

const MODOS = [
  ['AUTOMATICO', 'Automático', 'Happy Hour en su horario, demanda alta si se supera el umbral y, si no, precio normal.'],
  ['NORMAL', 'Normal', 'Siempre el precio base.'],
  ['HAPPY_HOUR', 'Happy Hour', 'Forzar el descuento ahora.'],
  ['DEMANDA_ALTA', 'Demanda alta', 'Forzar el recargo ahora.'],
];

function FormularioPolitica({ politica, onGuardar, ocupado }) {
  const [datos, setDatos] = useState(() => ({
    ...politica,
    inicioHappyHour: horaCorta(politica.inicioHappyHour),
    finHappyHour: horaCorta(politica.finHappyHour),
    horaSimulada: horaCorta(politica.horaSimulada),
  }));
  const cambiar = (campo) => (e) => setDatos((d) => ({ ...d, [campo]: e.target.value }));
  const enviar = (e) => {
    e.preventDefault();
    onGuardar({
      modo: datos.modo,
      inicioHappyHour: datos.inicioHappyHour,
      finHappyHour: datos.finHappyHour,
      descuentoHappyHour: Number(datos.descuentoHappyHour),
      incrementoDemandaAlta: Number(datos.incrementoDemandaAlta),
      umbralDemandaAlta: Number(datos.umbralDemandaAlta),
      margenMinimo: Number(datos.margenMinimo),
      horaSimulada: datos.horaSimulada || null,
    });
  };
  return (
    <form onSubmit={enviar}>
      <div className="field">
        <label>Modo</label>
        <div className="segmentado" role="radiogroup" aria-label="Modo de precios">
          {MODOS.map(([codigo, nombre]) => (
            <button key={codigo} type="button" role="radio" aria-checked={datos.modo === codigo}
                    className={datos.modo === codigo ? 'activo' : ''} onClick={() => setDatos((d) => ({ ...d, modo: codigo }))}>{nombre}</button>
          ))}
        </div>
        <small>{MODOS.find(([c]) => c === datos.modo)[2]}</small>
      </div>
      <div className="divider" />
      <div className="small strong mb8">PrecioHappyHour</div>
      <div className="fieldrow3">
        <div className="field"><label htmlFor="hhInicio">Desde</label><input id="hhInicio" type="time" required value={datos.inicioHappyHour} onChange={cambiar('inicioHappyHour')} /></div>
        <div className="field"><label htmlFor="hhFin">Hasta</label><input id="hhFin" type="time" required value={datos.finHappyHour} onChange={cambiar('finHappyHour')} /></div>
        <div className="field"><label htmlFor="hhDesc">Descuento %</label><input id="hhDesc" type="number" min="0" max="90" step="0.01" required value={datos.descuentoHappyHour} onChange={cambiar('descuentoHappyHour')} /></div>
      </div>
      <div className="field mt12">
        <label htmlFor="margen">Margen mínimo sobre el costo %</label>
        <input id="margen" type="number" min="0" max="1000" step="0.01" required value={datos.margenMinimo} onChange={cambiar('margenMinimo')} />
        <small>Con Happy Hour el precio nunca baja de costo × (1 + margen).</small>
      </div>
      <div className="divider" />
      <div className="small strong mb8">PrecioDemandaAlta</div>
      <div className="fieldrow">
        <div className="field"><label htmlFor="daInc">Recargo %</label><input id="daInc" type="number" min="0" max="100" step="0.01" required value={datos.incrementoDemandaAlta} onChange={cambiar('incrementoDemandaAlta')} /></div>
        <div className="field"><label htmlFor="daUmbral">Desde cuántas comandas en curso</label><input id="daUmbral" type="number" min="1" max="99" step="1" required value={datos.umbralDemandaAlta} onChange={cambiar('umbralDemandaAlta')} /></div>
      </div>
      <div className="divider" />
      <div className="field">
        <label htmlFor="simulada">Hora simulada para la demo (vacío = hora real)</label>
        <div className="flex gap8">
          <input id="simulada" type="time" value={datos.horaSimulada} onChange={cambiar('horaSimulada')} />
          <button className="btn btn-small" type="button" onClick={() => setDatos((d) => ({ ...d, horaSimulada: '' }))}>Usar hora real</button>
        </div>
      </div>
      <div className="actions-row mt16">
        <button className="btn btn-primary" type="submit" disabled={ocupado}>Guardar política de precios</button>
      </div>
    </form>
  );
}

export default function PreciosView() {
  const { establecimientoId, establecimiento, recargarEstablecimientos } = useSesion();
  const carta = useDatos(() => api.carta(establecimientoId), [establecimientoId], { intervalo: 15000 });
  const comparacion = useDatos(() => api.compararPrecios(establecimientoId), [establecimientoId]);
  const [ejecutar, ocupado] = useAccion();
  const [version, setVersion] = useState(0);
  const vigente = carta.datos?.estrategia;

  const guardar = async (politica) => {
    if (await ejecutar(() => api.establecimientos.configurarPrecios(establecimientoId, politica), 'Política de precios guardada')) {
      await recargarEstablecimientos();
      carta.recargar();
      comparacion.recargar();
      setVersion((v) => v + 1);
    }
  };

  return (
    <>
      <Encabezado titulo="Política de precios"
                  descripcion="Strategy: las tres estrategias son intercambiables en tiempo de ejecución. El precio de cada comanda se fija al registrarla y no se recalcula después." />
      <div className="cols-2" style={{ gridTemplateColumns: '1fr 1.1fr' }}>
        <div>
          <Tarjeta titulo="Estrategia vigente" subtitulo="La que se aplica a los pedidos que entren ahora">
            {vigente ? (
              <>
                <div className="flex gap12 wrap">
                  <span className="price">{vigente.nombre}</span>
                  <span className="tag mono">{vigente.clase}</span>
                </div>
                <div className="record-sub mt8">{vigente.motivo} · {horaCorta(vigente.hora)}{vigente.horaSimulada ? ' (hora simulada)' : ''}</div>
              </>
            ) : 'Cargando…'}
          </Tarjeta>
          <Tarjeta titulo="Configuración" subtitulo={establecimiento.nombre}>
            <FormularioPolitica key={`${establecimientoId}-${version}`} politica={establecimiento.politicaPrecio} onGuardar={guardar} ocupado={ocupado} />
          </Tarjeta>
        </div>
        <Tarjeta titulo="Comparación de estrategias" subtitulo="El mismo menú calculado por cada clase concreta">
          <Cargando estado={comparacion}>
            {(datos) => (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>Trago</th><th className="right">Costo</th>
                      {datos.estrategias.map((e) => (
                        <th key={e.codigo} className={`right${vigente?.codigo === e.codigo ? ' vigente' : ''}`}>{e.nombre}</th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {datos.filas.map((f) => (
                      <tr key={f.recetaId}>
                        <td>{f.icono} <b>{f.nombre}</b></td>
                        <td className="right muted">{pesos(f.costo)}</td>
                        {f.precios.map((precio, i) => (
                          <td key={datos.estrategias[i].codigo} className={`right${vigente?.codigo === datos.estrategias[i].codigo ? ' vigente strong' : ''}`}>{pesos(precio)}</td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </Cargando>
          <div className="note info mt16">La columna resaltada es la estrategia que hoy usa {establecimiento.nombre}. <Pill>{establecimiento.politicaPrecio.modo}</Pill></div>
        </Tarjeta>
      </div>
    </>
  );
}
