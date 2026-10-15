import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) {

        // ==============================
        // CREAR INSUMOS
        // ==============================

        Insumo gin =
                new Insumo(
                        "GIN001",
                        "Gin",
                        2000,
                        500,
                        0.8
                );

        Insumo tonica =
                new Insumo(
                        "TON001",
                        "Agua tónica",
                        5000,
                        1000,
                        0.15
                );

        Insumo limon =
                new Insumo(
                        "LIM001",
                        "Jugo de limón",
                        1000,
                        200,
                        0.10
                );


        // ==============================
        // INVENTARIO
        // ==============================

        Inventario inventario =
                new Inventario();

        inventario.agregarInsumo(gin);
        inventario.agregarInsumo(tonica);
        inventario.agregarInsumo(limon);


        // ==============================
        // OBSERVER
        // ==============================

        AlertaStock alerta =
                new AlertaStock(
                        "encargado@barstock.com",
                        "EMAIL"
                );

        inventario.agregarObserver(alerta);


        // ==============================
        // RECETA
        // ==============================

        Receta ginTonic =
                new Receta(
                        "Gin Tonic",
                        8000
                );

        ginTonic.agregarIngrediente(
                gin,
                50
        );

        ginTonic.agregarIngrediente(
                tonica,
                150
        );

        ginTonic.agregarIngrediente(
                limon,
                10
        );


        // ==============================
        // FACADE
        // ==============================

        BarStockFacade sistema =
                new BarStockFacade(
                        inventario
                );

        sistema.registrarReceta(
                ginTonic
        );


        // ==============================
        // STRATEGY
        // Happy Hour: 20 %
        // ==============================

        sistema.cambiarEstrategiaPrecio(
                new PrecioHappyHour(0.20)
        );


        // ==============================
        // STATE
        // ==============================

        int numeroComanda =
                sistema.crearComanda(
                        "Gin Tonic",
                        2
                );

        System.out.println(
                "Comanda creada: "
                        + numeroComanda
        );

        sistema.iniciarPreparacion(
                numeroComanda
        );


        // ==============================
        // DESPACHAR
        // ==============================

        double total =
                sistema.despachar(
                        numeroComanda
                );

        System.out.println(
                "Total cobrado: $"
                        + total
        );


        // ==============================
        // AUDITORÍA
        // ==============================

        Map<String, Double> stockFisico =
                new HashMap<>();

        stockFisico.put(
                "GIN001",
                1870.0
        );

        stockFisico.put(
                "TON001",
                4680.0
        );

        stockFisico.put(
                "LIM001",
                970.0
        );

        AuditoriaService auditoria =
                new AuditoriaService();

        List<Merma> mermas =
                auditoria.auditar(
                        inventario,
                        stockFisico
                );


        // ==============================
        // DECORATOR
        // ==============================

        Reporte reporte =
                new ReporteBasico(
                        sistema.getVentas()
                );

        reporte =
                new ReporteConMargen(
                        reporte,
                        sistema.getVentas()
                );

        reporte =
                new ReporteConMermas(
                        reporte,
                        mermas
                );


        System.out.println();

        System.out.println(
                reporte.generar()
        );
    }
}