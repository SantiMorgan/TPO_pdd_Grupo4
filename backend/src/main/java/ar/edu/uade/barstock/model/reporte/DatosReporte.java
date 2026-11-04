package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.establecimiento.Turno;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import java.util.List;
import java.util.UUID;

/**
 * Datos que leen el reporte base y sus decoradores, ya filtrados por establecimiento, período y turno.
 * {@code despachadas} incluye las comandas devueltas después del despacho, ordenadas por fecha de despacho;
 * {@code despachadasPeriodoAnterior} son las del período anterior de igual duración (para las tendencias).
 */
public record DatosReporte(Periodo periodo, Turno turno, List<Establecimiento> establecimientos,
                           CalendarioJornadas calendario, List<Comanda> despachadas,
                           List<Comanda> despachadasPeriodoAnterior, List<MovimientoStock> compras,
                           List<Auditoria> auditorias) {
    public List<Comanda> vendidas() {
        return despachadas.stream().filter(Comanda::cuentaComoVenta).toList();
    }

    public String nombreDe(UUID establecimientoId) {
        return establecimientos.stream().filter(e -> e.getId().equals(establecimientoId))
                .map(Establecimiento::getNombre).findFirst().orElse("");
    }

    /** Los mismos datos, solo con lo que corresponde a un turno. */
    public DatosReporte soloTurno(Turno elegido) {
        return new DatosReporte(periodo, elegido, establecimientos, calendario,
                despachadas.stream().filter(c -> calendario.esDelTurno(c.getEstablecimientoId(), c.getDespachadaEn(), elegido)).toList(),
                despachadasPeriodoAnterior.stream().filter(c -> calendario.esDelTurno(c.getEstablecimientoId(), c.getDespachadaEn(), elegido)).toList(),
                compras.stream().filter(m -> calendario.esDelTurno(m.getEstablecimientoId(), m.getFecha(), elegido)).toList(),
                auditorias.stream().filter(a -> calendario.esDelTurno(a.getEstablecimientoId(), a.getFecha(), elegido)).toList());
    }
}
