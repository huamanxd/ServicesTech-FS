package pe.fullstack.servicesbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/** Reloj inyectable: permite probar fechas límite y retrasos sin depender de la hora real. */
@Configuration
public class Reloj {

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
