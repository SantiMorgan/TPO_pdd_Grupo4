package ar.edu.uade.barstock.model.establecimiento;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** La jornada va de 6:00 a 5:59 y cada operación cae en el turno en curso o en el más cercano. */
class HorarioAtencionTest {
    private final HorarioAtencion horario = HorarioAtencion.porDefecto(); // 12–16 y 20–2

    @Test
    void loQuePasaDespuesDeMedianocheEsDeLaJornadaAnterior() {
        assertThat(HorarioAtencion.jornadaDe(LocalDateTime.of(2026, 11, 2, 23, 0))).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(HorarioAtencion.jornadaDe(LocalDateTime.of(2026, 11, 3, 2, 0))).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(HorarioAtencion.jornadaDe(LocalDateTime.of(2026, 11, 3, 5, 59))).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(HorarioAtencion.jornadaDe(LocalDateTime.of(2026, 11, 3, 6, 0))).isEqualTo(LocalDate.of(2026, 11, 3));
    }

    @ParameterizedTest
    @CsvSource({"13:00,MEDIODIA", "10:00,MEDIODIA", "17:30,MEDIODIA", "19:00,NOCHE", "21:00,NOCHE",
            "01:30,NOCHE", "02:20,NOCHE", "05:00,NOCHE", "16:00,MEDIODIA", "20:00,NOCHE"})
    void cadaHoraCaeEnSuTurnoOEnElMasCercano(LocalTime hora, Turno esperado) {
        assertThat(horario.turnoDe(hora)).isEqualTo(esperado);
    }

    @Test
    void losHorariosTienenQueSeguirElOrdenDeLaJornada() {
        assertThatThrownBy(() -> new HorarioAtencion(LocalTime.of(20, 0), LocalTime.of(2, 0), LocalTime.of(12, 0), LocalTime.of(16, 0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new HorarioAtencion(LocalTime.of(12, 0), LocalTime.of(16, 0), LocalTime.of(20, 0), LocalTime.of(6, 0)))
                .isInstanceOf(IllegalArgumentException.class);
        var largo = new HorarioAtencion(LocalTime.of(11, 30), LocalTime.of(16, 0), LocalTime.of(16, 0), LocalTime.of(5, 0));
        assertThat(largo.turnoDe(LocalTime.of(4, 30))).isEqualTo(Turno.NOCHE);
    }
}
