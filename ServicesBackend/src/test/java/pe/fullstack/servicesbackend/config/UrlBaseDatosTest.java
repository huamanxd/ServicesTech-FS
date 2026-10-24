package pe.fullstack.servicesbackend.config;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class UrlBaseDatosTest {

    @Test
    void traduceLaUrlDeRenderAJdbc() {
        var props = UrlBaseDatos.traducir(Map.of(
                "DATABASE_URL", "postgresql://servicestech:p%40ss@dpg-abc-a:5432/servicestech?sslmode=require"));
        assertThat(props)
                .containsEntry("spring.datasource.url", "jdbc:postgresql://dpg-abc-a:5432/servicestech?sslmode=require")
                .containsEntry("spring.datasource.username", "servicestech")
                .containsEntry("spring.datasource.password", "p@ss");
    }

    @Test
    void dbUrlExplicitaTienePrioridad() {
        assertThat(UrlBaseDatos.traducir(Map.of("DATABASE_URL", "postgres://u:p@h/db", "DB_URL", "jdbc:postgresql://x/y")))
                .isEmpty();
    }

    @Test
    void respetaUsuarioYClaveExplicitos() {
        var props = UrlBaseDatos.traducir(Map.of("DATABASE_URL", "postgres://u:p@h/db", "DB_USER", "otro", "DB_PASSWORD", "x"));
        assertThat(props).containsOnlyKeys("spring.datasource.url");
        assertThat(props.get("spring.datasource.url")).isEqualTo("jdbc:postgresql://h/db");
    }

    @Test
    void sinDatabaseUrlNoHaceNada() {
        assertThat(UrlBaseDatos.traducir(Map.of())).isEmpty();
    }
}
