package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.establecimiento.HorarioAtencion;
import ar.edu.uade.barstock.model.establecimiento.Turno;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Arma la cadena de decoradores en tiempo de ejecución según las secciones que pide el dueño,
 * sobre los datos filtrados por establecimiento, período de jornadas y turno.
 */
@Service
public class GeneradorReportes {
    private final FuenteDatosReporte fuente;
    private final TrazaPatrones traza;
    private final Clock reloj;

    public GeneradorReportes(FuenteDatosReporte fuente, TrazaPatrones traza, Clock reloj) {
        this.fuente = fuente;
        this.traza = traza;
        this.reloj = reloj;
    }

    /** {@code turno} nulo reúne los dos turnos. */
    @Transactional(readOnly = true)
    public ContenidoReporte generar(List<Establecimiento> establecimientos, Periodo periodo, Turno turno,
                                    Collection<TipoSeccion> secciones) {
        DatosReporte datos = recopilar(establecimientos, periodo, turno);
        Reporte reporte = new ReporteBase(datos);
        for (TipoSeccion seccion : new LinkedHashSet<>(secciones)) {
            reporte = seccion.decorar(reporte, datos);
        }
        ContenidoReporte contenido = reporte.generar();
        if (!secciones.isEmpty()) {
            establecimientos.forEach(e -> traza.registrar(e.getId(), "Decorator",
                    String.join(" + ", contenido.getComposicion())));
        }
        return contenido;
    }

    /** La jornada completa (mediodía + noche, incluida la madrugada) y cada turno por separado. */
    @Transactional(readOnly = true)
    public ReporteDiario generarDiario(List<Establecimiento> establecimientos, LocalDate jornada) {
        Periodo periodo = Periodo.de(jornada);
        DatosReporte datos = recopilar(establecimientos, periodo, null);
        List<ReporteDiario.ResumenTurno> turnos = Arrays.stream(Turno.values())
                .map(turno -> new ReporteDiario.ResumenTurno(turno,
                        new ReporteBase(datos.soloTurno(turno)).generar().getIndicadores()))
                .toList();
        return new ReporteDiario(jornada, datos.calendario().inicio(jornada), datos.calendario().fin(periodo),
                new ReporteBase(datos).generar().getIndicadores(), turnos, ReporteConGrafico.ranking(datos));
    }

    public LocalDate jornadaActual() {
        return HorarioAtencion.jornadaDe(LocalDateTime.now(reloj));
    }

    private DatosReporte recopilar(List<Establecimiento> establecimientos, Periodo periodo, Turno turno) {
        CalendarioJornadas calendario = new CalendarioJornadas(reloj.getZone(), establecimientos);
        List<UUID> ids = establecimientos.stream().map(Establecimiento::getId).toList();
        Instant desde = calendario.inicio(periodo.desde());
        Instant hasta = calendario.fin(periodo);
        List<Comanda> comandas = fuente.comandasDespachadas(ids, calendario.inicio(periodo.anterior().desde()), hasta)
                .stream().filter(c -> calendario.esDelTurno(c.getEstablecimientoId(), c.getDespachadaEn(), turno))
                .toList();
        List<MovimientoStock> compras = fuente.compras(ids, desde, hasta).stream()
                .filter(m -> calendario.esDelTurno(m.getEstablecimientoId(), m.getFecha(), turno)).toList();
        List<Auditoria> auditorias = fuente.auditorias(ids, desde, hasta).stream()
                .filter(a -> calendario.esDelTurno(a.getEstablecimientoId(), a.getFecha(), turno)).toList();
        return new DatosReporte(periodo, turno, establecimientos, calendario,
                comandas.stream().filter(c -> !c.getDespachadaEn().isBefore(desde)).toList(),
                comandas.stream().filter(c -> c.getDespachadaEn().isBefore(desde)).toList(),
                compras, auditorias);
    }
}
