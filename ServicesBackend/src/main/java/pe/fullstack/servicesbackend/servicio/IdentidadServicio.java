package pe.fullstack.servicesbackend.servicio;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas;
import pe.fullstack.servicesbackend.dto.VistaMapper;
import pe.fullstack.servicesbackend.modelo.Rol;
import pe.fullstack.servicesbackend.modelo.Sesion;
import pe.fullstack.servicesbackend.modelo.Usuario;
import pe.fullstack.servicesbackend.repositorio.SesionRepositorio;
import pe.fullstack.servicesbackend.repositorio.UsuarioRepositorio;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

/** Registro de clientes, inicio y cierre de sesión. */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdentidadServicio {

    /** Portal desde el que se inicia sesión y roles que admite. */
    public enum Portal {
        CLIENTE(EnumSet.of(Rol.Codigo.CLIENTE)),
        PERSONAL(EnumSet.of(Rol.Codigo.TECNICO, Rol.Codigo.ADMINISTRADOR));

        private final Set<Rol.Codigo> roles;

        Portal(Set<Rol.Codigo> roles) {
            this.roles = roles;
        }

        public boolean admite(Usuario u) {
            return u.getRoles().stream().anyMatch(r -> roles.contains(r.getNombre()));
        }
    }

    static final String CREDENCIALES_INCORRECTAS = "Correo o contraseña incorrectos";

    private final UsuarioRepositorio usuarios;
    private final SesionRepositorio sesiones;
    private final PasswordEncoder encoder;
    private final JwtServicio jwt;
    private final CatalogoServicio catalogo;
    private final VistaMapper mapper;
    private final Clock reloj;

    @Value("${app.jwt.horas:8}")
    private long horasSesion = 8;

    /** Hash ficticio para que un correo inexistente tarde lo mismo que uno existente. */
    private String hashFicticio;

    public static String normalizarEmail(String email) {
        return email.strip().toLowerCase(Locale.ROOT);
    }

    public static String huella(String token) {
        try {
            byte[] b = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(b);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    /** Registro público: siempre crea un CLIENTE. */
    @Transactional
    public Respuestas.Acceso registrarCliente(Peticiones.Registro p) {
        Usuario u = crearUsuario(p.nombres(), p.apellidos(), p.dni(), p.email(), p.telefono(), p.clave(), Rol.Codigo.CLIENTE);
        return emitir(u);
    }

    /** Usado por el alta de personal y por el administrador inicial. */
    @Transactional
    public Usuario crearUsuario(String nombres, String apellidos, String dni, String email, String telefono,
                                String clave, Rol.Codigo rol) {
        String correo = normalizarEmail(email);
        if (usuarios.existsByEmailIgnoreCase(correo)) {
            throw NegocioExcepcion.conflicto("El correo ya está registrado");
        }
        if (dni != null && usuarios.existsByDni(dni)) {
            throw NegocioExcepcion.conflicto("El DNI ya está registrado");
        }
        Usuario u = new Usuario();
        u.setNombres(nombres.strip());
        u.setApellidos(apellidos.strip());
        u.setDni(dni);
        u.setEmail(correo);
        u.setTelefono(telefono == null || telefono.isBlank() ? null : telefono);
        u.setClaveHash(encoder.encode(clave));
        u.setFechaRegistro(reloj.instant());
        u.getRoles().add(catalogo.rol(rol));
        return usuarios.saveAndFlush(u);
    }

    @Transactional
    public Respuestas.Acceso ingresar(Peticiones.Acceso p, Portal portal) {
        Optional<Usuario> encontrado = usuarios.findByEmailIgnoreCase(normalizarEmail(p.email()));
        if (encontrado.isEmpty()) {
            encoder.matches(p.clave(), hashFicticio());
            throw new NegocioExcepcion(HttpStatus.UNAUTHORIZED, CREDENCIALES_INCORRECTAS);
        }
        Usuario u = encontrado.get();
        if (!encoder.matches(p.clave(), u.getClaveHash())) {
            throw new NegocioExcepcion(HttpStatus.UNAUTHORIZED, CREDENCIALES_INCORRECTAS);
        }
        // Solo después de validar la contraseña se informa el motivo concreto.
        if (!u.isActivo()) {
            throw NegocioExcepcion.prohibido("Tu cuenta está desactivada. Comunícate con el administrador");
        }
        if (!portal.admite(u)) {
            throw NegocioExcepcion.prohibido(portal == Portal.CLIENTE
                    ? "Esta cuenta es de personal: ingresa desde Acceso Personal"
                    : "Esta cuenta es de cliente: ingresa desde el Portal Cliente");
        }
        return emitir(u);
    }

    private Respuestas.Acceso emitir(Usuario u) {
        Instant ahora = reloj.instant();
        Instant limite = ahora.plus(Duration.ofHours(horasSesion));
        String token = jwt.generar(u, ahora, limite);
        Sesion s = new Sesion();
        s.setUsuario(u);
        s.setTokenHash(huella(token));
        s.setExpiraEn(limite);
        sesiones.save(s);
        return new Respuestas.Acceso(token, limite, mapper.perfil(u));
    }

    /** Usuario dueño de un token válido, firmado, no revocado y de una cuenta activa. */
    @Transactional(readOnly = true)
    public Optional<Usuario> porToken(String token) {
        return jwt.validar(token).flatMap(datos ->
                sesiones.findByTokenHashAndExpiraEnAfter(huella(token), reloj.instant())
                        .map(Sesion::getUsuario)
                        .filter(u -> u.isActivo() && u.getId().toString().equals(datos.getSubject())));
    }

    @Transactional
    public void salir(String token) {
        sesiones.eliminarPorHash(huella(token));
    }

    @Transactional
    public void revocarSesiones(Long usuarioId) {
        sesiones.eliminarDelUsuario(usuarioId);
    }

    /** Limpieza periódica de sesiones vencidas (cada hora). */
    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT5M")
    @Transactional
    public void limpiarSesionesVencidas() {
        int borradas = sesiones.eliminarVencidas(reloj.instant());
        if (borradas > 0) log.info("Sesiones vencidas eliminadas: {}", borradas);
    }

    private String hashFicticio() {
        if (hashFicticio == null) hashFicticio = encoder.encode("clave-ficticia-para-tiempos-constantes");
        return hashFicticio;
    }
}
