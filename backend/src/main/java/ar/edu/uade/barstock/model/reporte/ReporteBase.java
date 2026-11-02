package ar.edu.uade.barstock.model.reporte;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Componente concreto: ventas, costos, compras, mermas y devoluciones del período elegido. */
public class ReporteBase implements Reporte {
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final DatosReporte datos;

    public ReporteBase(DatosReporte datos) {
        this.datos = datos;
    }

    @Override
    public ContenidoReporte generar() {
        List<Comanda> vendidas = datos.vendidas();
        BigDecimal ventas = sumar(vendidas.stream().map(Comanda::ventaNeta).toList());
        BigDecimal costo = sumar(datos.despachadas().stream().map(Comanda::getCostoTotal).toList());
        BigDecimal contribucion = ventas.subtract(costo);
        BigDecimal margen = ventas.signum() == 0 ? null
                : contribucion.multiply(CIEN).divide(ventas, 1, RoundingMode.HALF_UP);
        BigDecimal ticket = vendidas.isEmpty() ? BigDecimal.ZERO.setScale(2)
                : ventas.divide(BigDecimal.valueOf(vendidas.size()), 2, RoundingMode.HALF_UP);
        Indicadores indicadores = new Indicadores(
                ventas, vendidas.size(), vendidas.stream().mapToInt(Comanda::unidadesVendidas).sum(), ticket,
                costo, contribucion, margen,
                sumar(datos.compras().stream().map(MovimientoStock::getMonto).toList()),
                sumar(datos.auditorias().stream().map(Auditoria::getMermaTotal).toList()),
                sumar(datos.despachadas().stream().map(Comanda::importeDevuelto).toList()),
                datos.despachadas().stream().mapToInt(Comanda::unidadesDevueltas).sum(),
                datos.auditorias().size());
        return new ContenidoReporte(indicadores, datos.periodo(), datos.turno(),
                datos.establecimientos().stream().map(Establecimiento::getNombre).toList());
    }

    static BigDecimal sumar(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }
}
