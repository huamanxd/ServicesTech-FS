package pe.fullstack.servicesbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import pe.fullstack.servicesbackend.config.UrlBaseDatos;

@SpringBootApplication
@EnableScheduling
public class ServicesbackendApplication {

    public static void main(String[] args) {
        // Render entrega DATABASE_URL (postgres://...); Spring necesita una URL JDBC.
        UrlBaseDatos.aplicar(System.getenv());
        SpringApplication.run(ServicesbackendApplication.class, args);
    }
}
