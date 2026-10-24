package pe.fullstack.servicesbackend.servicio;

import org.junit.jupiter.api.Test;
import pe.fullstack.servicesbackend.modelo.Rol;
import pe.fullstack.servicesbackend.modelo.Usuario;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServicioTest {

    private static final String SECRETO = Base64.getEncoder()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));

    @Test
    void aceptaBase64Base64UrlYTextoPlanoDeAlMenos32Bytes() {
        byte[] bytes = new byte[32];
        bytes[0] = (byte) 0xfb; // produce '+' o '-' según el alfabeto
        assertThat(JwtServicio.bytesDelSecreto(Base64.getEncoder().encodeToString(bytes))).hasSize(32);
        assertThat(JwtServicio.bytesDelSecreto(Base64.getUrlEncoder().encodeToString(bytes))).hasSize(32);
        assertThat(JwtServicio.bytesDelSecreto("una-frase-secreta-de-mas-de-32-caracteres!")).hasSizeGreaterThanOrEqualTo(32);
    }

    @Test
    void rechazaSecretosVaciosOCortos() {
        assertThatThrownBy(() -> JwtServicio.bytesDelSecreto("")).hasMessageContaining("APP_JWT_SECRET");
        assertThatThrownBy(() -> JwtServicio.bytesDelSecreto("corto")).hasMessageContaining("32 bytes");
    }

    @Test
    void generaYValidaConRoles() {
        JwtServicio jwt = new JwtServicio(SECRETO);
        Usuario u = new Usuario();
        u.setId(7L);
        Rol r = new Rol();
        r.setNombre(Rol.Codigo.TECNICO);
        u.getRoles().add(r);
        Instant ahora = Instant.now();
        String token = jwt.generar(u, ahora, ahora.plusSeconds(60));

        var datos = jwt.validar(token).orElseThrow();
        assertThat(datos.getSubject()).isEqualTo("7");
        assertThat(datos.get("roles", java.util.List.class)).containsExactly("TECNICO");
        assertThat(jwt.validar(token + "x")).isEmpty();
        assertThat(new JwtServicio(Base64.getEncoder().encodeToString(new byte[32])).validar(token)).isEmpty();
    }
}
