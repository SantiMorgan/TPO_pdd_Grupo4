import java.util.List;

public class ReporteConMermas
        extends ReporteDecorator {

    private List<Merma> mermas;

    public ReporteConMermas(
            Reporte reporte,
            List<Merma> mermas) {

        super(reporte);
        this.mermas = mermas;
    }

    @Override
    public String generar() {

        StringBuilder texto =
                new StringBuilder(
                        reporte.generar()
                );

        texto.append(
                "\n\n=== MERMAS ===\n"
        );

        for (Merma merma : mermas) {

            texto.append(
                    merma.getInsumo()
            );

            texto.append(
                    ": "
            );

            texto.append(
                    merma.getDiferencia()
            );

            texto.append(
                    " ml\n"
            );
        }

        return texto.toString();
    }
}