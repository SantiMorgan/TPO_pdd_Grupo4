package ar.edu.uade.barstock.controller.error;

import ar.edu.uade.barstock.controller.InventarioController;
import ar.edu.uade.barstock.model.inventario.CodigoInsumoDuplicadoException;
import ar.edu.uade.barstock.model.inventario.InsumoInactivoException;
import ar.edu.uade.barstock.model.inventario.InsumoNoEncontradoException;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = InventarioController.class)
public class InventarioExceptionHandler {
    @ExceptionHandler(InsumoNoEncontradoException.class)
    public ProblemDetail noEncontrado(InsumoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({CodigoInsumoDuplicadoException.class, InsumoInactivoException.class})
    public ProblemDetail conflicto(RuntimeException ex) {
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridad(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "Los datos entran en conflicto con un registro existente o una restricción de integridad");
    }

    @ExceptionHandler(ConcurrencyFailureException.class)
    public ProblemDetail concurrencia(ConcurrencyFailureException ex) {
        return problema(HttpStatus.CONFLICT, "El insumo fue modificado por otra operación; volvé a intentarlo");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail validacion(MethodArgumentNotValidException ex) {
        ProblemDetail detalle = problema(HttpStatus.BAD_REQUEST, "Revisá los campos de la solicitud");
        Map<String, String> errores = new TreeMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.putIfAbsent(error.getField(), error.getDefaultMessage()));
        detalle.setProperty("errores", errores);
        return detalle;
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    public ProblemDetail formato(Exception ex) {
        return problema(HttpStatus.BAD_REQUEST, "Formato inválido: verificá el JSON, los tipos de datos, el UUID y la unidad (ML o UNIDAD)");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumento(IllegalArgumentException ex) {
        return problema(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ProblemDetail problema(HttpStatus estado, String detalle) {
        return ProblemDetail.forStatusAndDetail(estado, detalle);
    }
}
