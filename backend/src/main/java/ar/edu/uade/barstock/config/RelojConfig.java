package ar.edu.uade.barstock.config;

import java.time.Clock;
import java.time.ZoneId;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj inyectable: las pruebas fijan la hora para verificar Happy Hour sin esperar. */
@Configuration
public class RelojConfig {
    @Bean
    public Clock reloj(@Value("${barstock.zona-horaria:America/Argentina/Buenos_Aires}") String zona) {
        return Clock.system(ZoneId.of(zona));
    }
}
