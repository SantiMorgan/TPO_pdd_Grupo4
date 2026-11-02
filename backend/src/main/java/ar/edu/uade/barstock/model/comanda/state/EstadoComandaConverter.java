package ar.edu.uade.barstock.model.comanda.state;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Persiste el estado por su código y lo reconstruye como objeto State al leer la comanda. */
@Converter
public class EstadoComandaConverter implements AttributeConverter<EstadoComanda, String> {
    @Override
    public String convertToDatabaseColumn(EstadoComanda estado) {
        return estado == null ? null : estado.codigo();
    }

    @Override
    public EstadoComanda convertToEntityAttribute(String codigo) {
        return codigo == null ? null : EstadosComanda.desde(codigo);
    }
}
