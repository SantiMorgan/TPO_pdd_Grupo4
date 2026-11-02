package ar.edu.uade.barstock.facade;

import ar.edu.uade.barstock.model.auditoria.Auditoria;
import ar.edu.uade.barstock.model.auditoria.AuditoriaService;
import ar.edu.uade.barstock.model.comanda.Comanda;
import ar.edu.uade.barstock.model.comanda.ComandaService;
import ar.edu.uade.barstock.model.comanda.OrigenComanda;
import ar.edu.uade.barstock.model.establecimiento.ConfiguracionAlertas;
import ar.edu.uade.barstock.model.establecimiento.Establecimiento;
import ar.edu.uade.barstock.model.establecimiento.EstablecimientoService;
import ar.edu.uade.barstock.model.establecimiento.PoliticaPrecio;
import ar.edu.uade.barstock.model.inventario.AjusteStock;
import ar.edu.uade.barstock.model.inventario.Insumo;
import ar.edu.uade.barstock.model.inventario.Inventario;
import ar.edu.uade.barstock.model.inventario.InventarioService;
import ar.edu.uade.barstock.model.inventario.MovimientoStock;
import ar.edu.uade.barstock.model.inventario.UnidadMedida;
import ar.edu.uade.barstock.model.notificacion.BitacoraNotificaciones;
import ar.edu.uade.barstock.model.notificacion.RegistroNotificacion;
import ar.edu.uade.barstock.model.precio.Carta;
import ar.edu.uade.barstock.model.precio.ComparacionPrecios;
import ar.edu.uade.barstock.model.precio.EstrategiaVigente;
import ar.edu.uade.barstock.model.precio.SelectorEstrategiaPrecio;
import ar.edu.uade.barstock.model.receta.CantidadReceta;
import ar.edu.uade.barstock.model.receta.Receta;
import ar.edu.uade.barstock.model.receta.RecetaService;
import ar.edu.uade.barstock.model.reporte.ContenidoReporte;
import ar.edu.uade.barstock.model.reporte.GeneradorReportes;
import ar.edu.uade.barstock.model.reporte.TipoSeccion;
import ar.edu.uade.barstock.model.traza.EventoPatron;
import ar.edu.uade.barstock.model.traza.TrazaPatrones;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Facade: punto de entrada único de los controladores a los casos de uso.
 * Solo orquesta los subsistemas (establecimientos, inventario, recetas, comandas, precios,
 * auditoría y reportes) y define la transacción de cada caso; las reglas viven en el modelo.
 */
@Service
public class BarStockFacade {
    private final EstablecimientoService establecimientos;
    private final InventarioService insumos;
    private final Inventario inventario;
    private final RecetaService recetas;
    private final ComandaService comandas;
    private final SelectorEstrategiaPrecio precios;
    private final AuditoriaService auditorias;
    private final GeneradorReportes reportes;
    private final BitacoraNotificaciones notificaciones;
    private final TrazaPatrones traza;

    public BarStockFacade(EstablecimientoService establecimientos, InventarioService insumos, Inventario inventario,
                          RecetaService recetas, ComandaService comandas, SelectorEstrategiaPrecio precios,
                          AuditoriaService auditorias, GeneradorReportes reportes,
                          BitacoraNotificaciones notificaciones, TrazaPatrones traza) {
        this.establecimientos = establecimientos;
        this.insumos = insumos;
        this.inventario = inventario;
        this.recetas = recetas;
        this.comandas = comandas;
        this.precios = precios;
        this.auditorias = auditorias;
        this.reportes = reportes;
        this.notificaciones = notificaciones;
        this.traza = traza;
    }

    public String obtenerNombreAplicacion() {
        return "BarStock Solutions";
    }

    // ── Establecimientos ──────────────────────────────────────────────

    public List<Establecimiento> listarEstablecimientos() {
        return establecimientos.listar();
    }

    public Establecimiento consultarEstablecimiento(UUID id) {
        return establecimientos.consultar(id);
    }

    public Establecimiento crearEstablecimiento(String codigo, String nombre, String zona, String descripcion) {
        return establecimientos.crear(codigo, nombre, zona, descripcion);
    }

    public Establecimiento editarEstablecimiento(UUID id, String nombre, String zona, String descripcion) {
        return establecimientos.editar(id, nombre, zona, descripcion);
    }

    public Establecimiento configurarPrecios(UUID id, PoliticaPrecio politica) {
        Establecimiento establecimiento = establecimientos.cambiarPoliticaPrecio(id, politica);
        traza.registrar(id, "Strategy", "El dueño configuró la política de precios en modo " + politica.getModo());
        return establecimiento;
    }

    public Establecimiento configurarAlertas(UUID id, ConfiguracionAlertas alertas) {
        return establecimientos.cambiarAlertas(id, alertas);
    }

    // ── Insumos, compras y movimientos ────────────────────────────────

    public Insumo crearInsumo(UUID establecimientoId, String codigo, String nombre, UnidadMedida unidadMedida,
                              BigDecimal stockInicial, BigDecimal costoUnitario, BigDecimal puntoReorden,
                              int contenidoEnvase) {
        establecimientos.verificarExistencia(establecimientoId);
        return insumos.crear(establecimientoId, codigo, nombre, unidadMedida, stockInicial, costoUnitario,
                puntoReorden, contenidoEnvase);
    }

    public List<Insumo> listarInsumos(UUID establecimientoId, boolean activo) {
        establecimientos.verificarExistencia(establecimientoId);
        return insumos.listar(establecimientoId, activo);
    }

    public Insumo consultarInsumo(UUID establecimientoId, UUID id) {
        return insumos.consultar(establecimientoId, id);
    }

    public Insumo editarInsumo(UUID establecimientoId, UUID id, String nombre, BigDecimal costoUnitario,
                               BigDecimal puntoReorden, Integer contenidoEnvase) {
        return insumos.editar(establecimientoId, id, nombre, costoUnitario, puntoReorden, contenidoEnvase);
    }

    public void darDeBajaInsumo(UUID establecimientoId, UUID id) {
        insumos.darDeBaja(establecimientoId, id);
    }

    public Insumo darDeAltaInsumo(UUID establecimientoId, UUID id) {
        return insumos.darDeAlta(establecimientoId, id);
    }

    @Transactional
    public MovimientoStock registrarCompra(UUID establecimientoId, UUID insumoId, int envases) {
        MovimientoStock compra = inventario.registrarCompra(establecimientoId, insumoId, envases);
        traza.registrar(establecimientoId, "Facade", "registrarCompra() → " + compra.getNombreInsumo() + " +"
                + compra.getCantidad().toPlainString() + " " + compra.getUnidadMedida().simbolo()
                + " por " + pesos(compra.getMonto()));
        return compra;
    }

    public List<MovimientoStock> listarMovimientos(UUID establecimientoId, int limite) {
        establecimientos.verificarExistencia(establecimientoId);
        return inventario.ultimosMovimientos(establecimientoId, limite);
    }

    // ── Recetas, carta y precios ──────────────────────────────────────

    public List<Receta> listarRecetas(UUID establecimientoId, boolean incluirFueraDelMenu) {
        establecimientos.verificarExistencia(establecimientoId);
        return recetas.listar(establecimientoId, incluirFueraDelMenu);
    }

    public Receta consultarReceta(UUID establecimientoId, UUID id) {
        return recetas.consultar(establecimientoId, id);
    }

    public Receta crearReceta(UUID establecimientoId, String nombre, String descripcion, String icono,
                              BigDecimal precioBase, Map<UUID, Integer> ingredientes) {
        establecimientos.verificarExistencia(establecimientoId);
        return recetas.crear(establecimientoId, nombre, descripcion, icono, precioBase, ingredientes);
    }

    public Receta editarReceta(UUID establecimientoId, UUID id, String nombre, String descripcion, String icono,
                               BigDecimal precioBase, Map<UUID, Integer> ingredientes) {
        return recetas.editar(establecimientoId, id, nombre, descripcion, icono, precioBase, ingredientes);
    }

    public Receta quitarRecetaDelMenu(UUID establecimientoId, UUID id) {
        return recetas.quitarDelMenu(establecimientoId, id);
    }

    public Receta volverRecetaAlMenu(UUID establecimientoId, UUID id) {
        return recetas.volverAlMenu(establecimientoId, id);
    }

    /** Menú con el precio vigente. Lo usan el cliente y el bartender. */
    @Transactional(readOnly = true)
    public Carta consultarCarta(UUID establecimientoId) {
        Establecimiento establecimiento = establecimientos.consultar(establecimientoId);
        EstrategiaVigente vigente = precios.vigente(establecimiento, comandas.contarEnCurso(establecimientoId));
        return Carta.armar(vigente, recetas.listar(establecimientoId, false));
    }

    @Transactional(readOnly = true)
    public ComparacionPrecios compararEstrategias(UUID establecimientoId) {
        Establecimiento establecimiento = establecimientos.consultar(establecimientoId);
        return ComparacionPrecios.de(precios.todas(establecimiento), recetas.listar(establecimientoId, false));
    }

    // ── Comandas ──────────────────────────────────────────────────────

    /** Registra sin reservar stock: verifica que hoy alcance y fija el precio con la estrategia vigente. */
    @Transactional
    public Comanda registrarComanda(UUID establecimientoId, OrigenComanda origen, String cliente,
                                    List<CantidadReceta> pedido) {
        Establecimiento establecimiento = establecimientos.bloquear(establecimientoId);
        Map<Receta, Integer> lineas = recetas.armarPedido(establecimientoId, pedido);
        inventario.verificarDisponibilidad(establecimientoId, recetas.consumosDe(establecimientoId, pedido));
        EstrategiaVigente vigente = precios.vigente(establecimiento, comandas.contarEnCurso(establecimientoId));
        Comanda comanda = comandas.registrar(establecimiento, origen, cliente, vigente.estrategia(), lineas);
        traza.registrar(establecimientoId, "Strategy", "Precio fijado con " + vigente.estrategia().getClass().getSimpleName()
                + " (" + vigente.motivo() + "): " + pesos(comanda.getTotal()));
        traza.registrar(establecimientoId, "Facade", "registrarComanda() → #" + comanda.getNumero() + ", "
                + comanda.unidades() + " trago(s), pedido por " + origen.name().toLowerCase(Locale.ROOT));
        return comanda;
    }

    @Transactional
    public Comanda prepararComanda(UUID establecimientoId, UUID comandaId) {
        return comandas.preparar(establecimientoId, comandaId);
    }

    /** Una sola transacción: cambia el estado y descuenta todos los insumos, o no hace nada. */
    @Transactional
    public Comanda despacharComanda(UUID establecimientoId, UUID comandaId) {
        Comanda comanda = comandas.despachar(establecimientoId, comandaId);
        List<CantidadReceta> pedido = comanda.cantidadesPorReceta();
        inventario.descontarTodo(establecimientoId, recetas.consumosDe(establecimientoId, pedido),
                "Comanda #" + comanda.getNumero());
        traza.registrar(establecimientoId, "Facade", "despacharComanda(#" + comanda.getNumero()
                + "): Comandas + Recetas + Inventario, descuento atómico por " + pesos(comanda.getTotal()));
        return comanda;
    }

    @Transactional
    public Comanda anularComanda(UUID establecimientoId, UUID comandaId, String motivo) {
        Comanda comanda = comandas.anular(establecimientoId, comandaId, motivo);
        traza.registrar(establecimientoId, "Facade", comanda.fueDevuelta()
                ? "anularComanda(#" + comanda.getNumero() + "): devolución, se revierte el ingreso sin reponer insumos"
                : "anularComanda(#" + comanda.getNumero() + "): sin cambios en el inventario");
        return comanda;
    }

    public Comanda consultarComanda(UUID establecimientoId, UUID comandaId) {
        return comandas.consultar(establecimientoId, comandaId);
    }

    public List<Comanda> listarComandasEnCurso(UUID establecimientoId) {
        establecimientos.verificarExistencia(establecimientoId);
        return comandas.listarEnCurso(establecimientoId);
    }

    public List<Comanda> listarComandasRecientes(UUID establecimientoId, int limite) {
        establecimientos.verificarExistencia(establecimientoId);
        return comandas.listarRecientes(establecimientoId, limite);
    }

    // ── Auditoría ─────────────────────────────────────────────────────

    @Transactional
    public Auditoria auditarInventario(UUID establecimientoId, String responsable, Map<UUID, BigDecimal> recuento) {
        Establecimiento establecimiento = establecimientos.bloquear(establecimientoId);
        List<AjusteStock> ajustes = inventario.ajustarARecuento(establecimientoId, recuento,
                "Recuento físico #" + establecimiento.verProximoNumeroAuditoria());
        Auditoria auditoria = auditorias.registrar(establecimiento, responsable, ajustes);
        traza.registrar(establecimientoId, "Facade", "auditarInventario() → cierre #" + auditoria.getNumero() + ": "
                + ajustes.size() + " insumo(s), merma " + pesos(auditoria.getMermaTotal()));
        return auditoria;
    }

    public List<Auditoria> listarAuditorias(UUID establecimientoId, int limite) {
        establecimientos.verificarExistencia(establecimientoId);
        return auditorias.listar(establecimientoId, limite);
    }

    // ── Reportes, alertas y traza ─────────────────────────────────────

    public ContenidoReporte generarReporte(UUID establecimientoId, List<TipoSeccion> secciones) {
        establecimientos.verificarExistencia(establecimientoId);
        return reportes.generar(establecimientoId, secciones);
    }

    public List<RegistroNotificacion> listarNotificaciones(UUID establecimientoId, int limite) {
        establecimientos.verificarExistencia(establecimientoId);
        return notificaciones.ultimas(establecimientoId, limite);
    }

    public List<EventoPatron> consultarTraza(UUID establecimientoId) {
        establecimientos.verificarExistencia(establecimientoId);
        return traza.consultar(establecimientoId);
    }

    private static String pesos(BigDecimal monto) {
        NumberFormat formato = NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-AR"));
        return "$ " + formato.format(monto.setScale(0, RoundingMode.HALF_UP));
    }
}
