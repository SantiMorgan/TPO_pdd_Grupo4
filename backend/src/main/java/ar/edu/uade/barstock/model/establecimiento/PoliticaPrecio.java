package ar.edu.uade.barstock.model.establecimiento;

import ar.edu.uade.barstock.model.Validacion;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalTime;

/** Parámetros con los que se construyen las estrategias de precio de un establecimiento. */
@Embeddable
public class PoliticaPrecio {
    @Enumerated(EnumType.STRING)
    @Column(name = "modo_precio", nullable = false, length = 20)
    private ModoPrecio modo;

    @Column(name = "happy_hour_inicio", nullable = false)
    private LocalTime inicioHappyHour;

    @Column(name = "happy_hour_fin", nullable = false)
    private LocalTime finHappyHour;

    @Column(name = "happy_hour_descuento", nullable = false, precision = 5, scale = 2)
    private BigDecimal descuentoHappyHour;

    @Column(name = "demanda_alta_incremento", nullable = false, precision = 5, scale = 2)
    private BigDecimal incrementoDemandaAlta;

    @Column(name = "demanda_alta_umbral", nullable = false)
    private int umbralDemandaAlta;

    @Column(name = "margen_minimo", nullable = false, precision = 6, scale = 2)
    private BigDecimal margenMinimo;

    /** Solo para demostraciones: reemplaza la hora real al elegir la estrategia. */
    @Column(name = "hora_simulada")
    private LocalTime horaSimulada;

    protected PoliticaPrecio() {
        // Constructor requerido por JPA.
    }

    public PoliticaPrecio(ModoPrecio modo, LocalTime inicioHappyHour, LocalTime finHappyHour,
                          BigDecimal descuentoHappyHour, BigDecimal incrementoDemandaAlta,
                          int umbralDemandaAlta, BigDecimal margenMinimo, LocalTime horaSimulada) {
        if (modo == null || inicioHappyHour == null || finHappyHour == null) {
            throw new IllegalArgumentException("El modo y el horario de Happy Hour son obligatorios");
        }
        this.modo = modo;
        this.inicioHappyHour = inicioHappyHour.withSecond(0).withNano(0);
        this.finHappyHour = finHappyHour.withSecond(0).withNano(0);
        this.descuentoHappyHour = Validacion.porcentaje(descuentoHappyHour, 90, "descuentoHappyHour");
        this.incrementoDemandaAlta = Validacion.porcentaje(incrementoDemandaAlta, 100, "incrementoDemandaAlta");
        this.umbralDemandaAlta = Validacion.entero(umbralDemandaAlta, 1, 99, "umbralDemandaAlta");
        this.margenMinimo = Validacion.porcentaje(margenMinimo, 1000, "margenMinimo");
        this.horaSimulada = horaSimulada == null ? null : horaSimulada.withSecond(0).withNano(0);
    }

    public static PoliticaPrecio porDefecto() {
        return new PoliticaPrecio(ModoPrecio.AUTOMATICO, LocalTime.of(18, 0), LocalTime.of(21, 0),
                new BigDecimal("20"), new BigDecimal("15"), 4, new BigDecimal("50"), null);
    }

    /** El intervalo es [inicio, fin) y puede cruzar la medianoche (por ejemplo, de 23:00 a 01:00). */
    public boolean esHappyHour(LocalTime hora) {
        if (inicioHappyHour.equals(finHappyHour)) {
            return false;
        }
        if (inicioHappyHour.isBefore(finHappyHour)) {
            return !hora.isBefore(inicioHappyHour) && hora.isBefore(finHappyHour);
        }
        return !hora.isBefore(inicioHappyHour) || hora.isBefore(finHappyHour);
    }

    public LocalTime horaDeReferencia(LocalTime horaReal) {
        return horaSimulada != null ? horaSimulada : horaReal;
    }

    public ModoPrecio getModo() { return modo; }
    public LocalTime getInicioHappyHour() { return inicioHappyHour; }
    public LocalTime getFinHappyHour() { return finHappyHour; }
    public BigDecimal getDescuentoHappyHour() { return descuentoHappyHour; }
    public BigDecimal getIncrementoDemandaAlta() { return incrementoDemandaAlta; }
    public int getUmbralDemandaAlta() { return umbralDemandaAlta; }
    public BigDecimal getMargenMinimo() { return margenMinimo; }
    public LocalTime getHoraSimulada() { return horaSimulada; }
}
