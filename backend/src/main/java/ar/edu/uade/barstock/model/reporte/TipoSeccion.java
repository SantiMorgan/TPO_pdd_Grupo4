package ar.edu.uade.barstock.model.reporte;

/** Secciones opcionales del reporte. Cada una sabe con qué decorador envolver el reporte recibido. */
public enum TipoSeccion {
    GRAFICO {
        @Override
        public Reporte decorar(Reporte reporte, DatosReporte datos) {
            return new ReporteConGrafico(reporte, datos);
        }
    },
    TENDENCIAS {
        @Override
        public Reporte decorar(Reporte reporte, DatosReporte datos) {
            return new ReporteConTendencias(reporte, datos);
        }
    },
    MERMAS {
        @Override
        public Reporte decorar(Reporte reporte, DatosReporte datos) {
            return new ReporteConMermas(reporte, datos);
        }
    },
    MARGEN {
        @Override
        public Reporte decorar(Reporte reporte, DatosReporte datos) {
            return new ReporteConMargen(reporte, datos);
        }
    };

    public abstract Reporte decorar(Reporte reporte, DatosReporte datos);
}
