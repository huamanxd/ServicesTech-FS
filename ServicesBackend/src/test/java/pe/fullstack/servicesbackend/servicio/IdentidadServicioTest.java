package pe.fullstack.servicesbackend.servicio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.VistaMapper;
import pe.fullstack.servicesbackend.modelo.Rol;
import pe.fullstack.servicesbackend.modelo.Sesion;
import pe.fullstack.servicesbackend.modelo.Usuario;
import pe.fullstack.servicesbackend.repositorio.SesionRepositorio;
import pe.fullstack.servicesbackend.repositorio.UsuarioRepositorio;
import pe.fullstack.servicesbackend.servicio.IdentidadServicio.Portal;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.Base64;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Reglas de acceso sin base de datos (repositorios simulados con Mockito). */
class IdentidadServicioTest {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private UsuarioRepositorio usuarios;
    private SesionRepositorio sesiones;
    private CatalogoServicio catalogo;
    private IdentidadServicio identidad;

    @BeforeEach
    void preparar() {
        usuarios = mock(UsuarioRepositorio.class);
        sesiones = mock(SesionRepositorio.class);
        catalogo = mock(CatalogoServicio.class);
        for (Rol.Codigo c : Rol.Codigo.values()) when(catalogo.rol(c)).thenReturn(rol(c));
        String secreto = Base64.getEncoder().encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8));
        identidad = new IdentidadServicio(usuarios, sesiones, encoder, new JwtServicio(secreto),
                catalogo, Mappers.getMapper(VistaMapper.class), Clock.systemUTC());
    }

    private static Rol rol(Rol.Codigo c) {
        Rol r = new Rol();
        r.setNombre(c);
        return r;
    }

    private Usuario usuario(Rol.Codigo c, boolean activo) {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setNombres("Ana");
        u.setApellidos("Rojas");
        u.setEmail("ana@ejemplo.com");
        u.setActivo(activo);
        u.setClaveHash(encoder.encode("ClaveRobusta!2026"));
        u.getRoles().add(rol(c));
        return u;
    }

    @Test
    void registroPublicoSiempreCreaClienteConClaveCifrada() {
        when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        var acceso = identidad.registrarCliente(new Peticiones.Registro("Ana", "Rojas", "12345678", "ANA@Ejemplo.com ",
                "987654321", "ClaveRobusta!2026"));

        assertThat(acceso.usuario().roles()).containsExactly("CLIENTE");
        assertThat(acceso.usuario().email()).isEqualTo("ana@ejemplo.com");
        assertThat(acceso.token()).isNotBlank();
        verify(usuarios).saveAndFlush(argThat(u -> encoder.matches("ClaveRobusta!2026", u.getClaveHash())));
        verify(sesiones).save(any(Sesion.class));
    }

    @Test
    void correoODniDuplicadoDaConflicto() {
        when(usuarios.existsByEmailIgnoreCase("ana@ejemplo.com")).thenReturn(true);
        var p = new Peticiones.Registro("Ana", "Rojas", "12345678", "ana@ejemplo.com", "987654321", "ClaveRobusta!2026");
        assertThatThrownBy(() -> identidad.registrarCliente(p))
                .isInstanceOfSatisfying(NegocioExcepcion.class, e -> assertThat(e.getEstado()).isEqualTo(HttpStatus.CONFLICT));
        verifyNoInteractions(sesiones);
    }

    @Test
    void credencialesIncorrectasUsanElMismoMensajeExistaONoElCorreo() {
        when(usuarios.findByEmailIgnoreCase("ana@ejemplo.com")).thenReturn(Optional.of(usuario(Rol.Codigo.CLIENTE, true)));
        var malaClave = assertThatThrownBy(() -> identidad.ingresar(new Peticiones.Acceso("ana@ejemplo.com", "otra"), Portal.CLIENTE));
        var sinCuenta = assertThatThrownBy(() -> identidad.ingresar(new Peticiones.Acceso("nadie@ejemplo.com", "otra"), Portal.CLIENTE));
        malaClave.hasMessage(IdentidadServicio.CREDENCIALES_INCORRECTAS);
        sinCuenta.hasMessage(IdentidadServicio.CREDENCIALES_INCORRECTAS);
    }

    @Test
    void clienteNoEntraPorElPortalDePersonalNiCuentaInactiva() {
        when(usuarios.findByEmailIgnoreCase("ana@ejemplo.com")).thenReturn(Optional.of(usuario(Rol.Codigo.CLIENTE, true)));
        assertThatThrownBy(() -> identidad.ingresar(new Peticiones.Acceso("ana@ejemplo.com", "ClaveRobusta!2026"), Portal.PERSONAL))
                .isInstanceOfSatisfying(NegocioExcepcion.class, e -> assertThat(e.getEstado()).isEqualTo(HttpStatus.FORBIDDEN));

        when(usuarios.findByEmailIgnoreCase("ana@ejemplo.com")).thenReturn(Optional.of(usuario(Rol.Codigo.CLIENTE, false)));
        assertThatThrownBy(() -> identidad.ingresar(new Peticiones.Acceso("ana@ejemplo.com", "ClaveRobusta!2026"), Portal.CLIENTE))
                .hasMessageContaining("desactivada");
        verifyNoInteractions(sesiones);
    }

    @Test
    void tecnicoYAdministradorEntranPorElPortalDePersonal() {
        assertThat(Portal.PERSONAL.admite(usuario(Rol.Codigo.TECNICO, true))).isTrue();
        assertThat(Portal.PERSONAL.admite(usuario(Rol.Codigo.ADMINISTRADOR, true))).isTrue();
        assertThat(Portal.CLIENTE.admite(usuario(Rol.Codigo.TECNICO, true))).isFalse();
    }
}
