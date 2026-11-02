package ar.edu.uade.barstock.controller.error;

import ar.edu.uade.barstock.model.exception.RecursoNoEncontradoException;
import ar.edu.uade.barstock.model.exception.ReglaDeNegocioException;
import ar.edu.uade.barstock.model.inventario.Faltante;
import ar.edu.uade.barstock.model.inventario.StockInsuficienteException;
import ar.edu.uade.barstock.model.inventario.UnidadIncompatibleException;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Traduce las excepciones de dominio y de formato a respuestas RFC 7807 (ProblemDetail). */
@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ProblemDetail stockInsuficiente(StockInsuficienteException ex) {
        ProblemDetail detalle = problema(HttpStatus.CONFLICT, ex.getMessage());
        detalle.setProperty("faltantes", ex.getFaltantes().stream().map(Faltante::describir).toList());
        return detalle;
    }

    @ExceptionHandler(ReglaDeNegocioException.class)
    public ProblemDetail reglaDeNegocio(ReglaDeNegocioException ex) {
        log.warn("Operación rechazada: {}", ex.getMessage());
        return problema(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(UnidadIncompatibleException.class)
    public ProblemDetail unidadIncompatible(UnidadIncompatibleException ex) {
        return problema(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail integridad(DataIntegrityViolationException ex) {
        return problema(HttpStatus.CONFLICT, "Los datos entran en conflicto con un registro existente o una restricción de integridad");
    }

    @ExceptionHandler({ConcurrencyFailureException.class, ObjectOptimisticLockingFailureException.class})
    public ProblemDetail concurrencia(RuntimeException ex) {
        return problema(HttpStatus.CONFLICT, "El registro fue modificado por otra operación; volvé a intentarlo");
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

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    public ProblemDetail formato(Exception ex) {
        return problema(HttpStatus.BAD_REQUEST,
                "Formato inválido: verificá el JSON, los tipos de datos, los identificadores y los valores permitidos");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail argumento(IllegalArgumentException ex) {
        return problema(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    private ProblemDetail problema(HttpStatus estado, String detalle) {
        return ProblemDetail.forStatusAndDetail(estado, detalle);
    }
}
