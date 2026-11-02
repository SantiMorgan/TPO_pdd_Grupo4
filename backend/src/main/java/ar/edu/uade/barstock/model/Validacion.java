package ar.edu.uade.barstock.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Validaciones de formato compartidas por las entidades. Lanzan IllegalArgumentException (HTTP 400). */
public final class Validacion {
    private Validacion() {
    }

    public static String texto(String valor, int longitudMaxima, String campo) {
        if (valor == null || valor.isBlank() || valor.strip().length() > longitudMaxima) {
            throw new IllegalArgumentException(campo + " es obligatorio y admite hasta " + longitudMaxima + " caracteres");
        }
        return valor.strip();
    }

    public static String textoOpcional(String valor, int longitudMaxima, String campo) {
        if (valor == null || valor.isBlank()) {
            return "";
        }
        if (valor.strip().length() > longitudMaxima) {
            throw new IllegalArgumentException(campo + " admite hasta " + longitudMaxima + " caracteres");
        }
        return valor.strip();
    }

    public static BigDecimal porcentaje(BigDecimal valor, int maximo, String campo) {
        if (valor == null || valor.signum() < 0 || valor.compareTo(BigDecimal.valueOf(maximo)) > 0) {
            throw new IllegalArgumentException(campo + " debe estar entre 0 y " + maximo);
        }
        try {
            return valor.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(campo + " admite hasta 2 decimales", ex);
        }
    }

    public static int entero(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(campo + " debe estar entre " + minimo + " y " + maximo);
        }
        return valor;
    }
}
