package pe.fullstack.servicesbackend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import pe.fullstack.servicesbackend.modelo.Rol;
import pe.fullstack.servicesbackend.repositorio.UsuarioRepositorio;
import pe.fullstack.servicesbackend.servicio.IdentidadServicio;

/**
 * Crea el primer ADMINISTRADOR a partir de ADMIN_EMAIL y ADMIN_PASSWORD si ese correo no existe.
 * Nunca modifica una cuenta existente (ni su rol ni su contraseña).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdministradorInicial implements CommandLineRunner {

    private final UsuarioRepositorio usuarios;
    private final IdentidadServicio identidad;

    @Value("${app.admin.email:}")
    private String email = "";

    @Value("${app.admin.password:}")
    private String clave = "";

    @Override
    public void run(String... args) {
        if (email.isBlank() && clave.isBlank()) return;
        if (email.isBlank() || clave.length() < 12) {
            throw new IllegalArgumentException("ADMIN_EMAIL y ADMIN_PASSWORD (mínimo 12 caracteres) se definen juntos");
        }
        String correo = IdentidadServicio.normalizarEmail(email);
        if (usuarios.existsByEmailIgnoreCase(correo)) return;
        identidad.crearUsuario("Administrador", "Inicial", null, correo, null, clave, Rol.Codigo.ADMINISTRADOR);
        log.info("Administrador inicial creado: {}", correo);
    }
}
