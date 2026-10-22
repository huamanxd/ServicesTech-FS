package pe.fullstack.servicesbackend.portal.servicio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpStatus;
import pe.fullstack.servicesbackend.portal.modelo.*;
import pe.fullstack.servicesbackend.portal.dto.*;
import pe.fullstack.servicesbackend.portal.repositorio.*;
import java.security.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.nio.charset.StandardCharsets;
@Service
public class IdentidadServicio {
 private final UsuarioRepositorio usuarios;
 private final SesionRepositorio sesiones;
 private final PasswordEncoder encoder;
 private final JwtServicio jwt;

 public IdentidadServicio(
    UsuarioRepositorio usuarios,
    SesionRepositorio sesiones,
    PasswordEncoder encoder,
    JwtServicio jwt
) {this.usuarios = usuarios;
    this.sesiones = sesiones;
    this.encoder = encoder;
    this.jwt = jwt;}
 public static String normaliza(String email){return email.strip().toLowerCase(Locale.ROOT);}
 public static String huella(String token){
  try { byte[] b=MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(b); }
  catch(NoSuchAlgorithmException ex){throw new IllegalStateException(ex);}
 }
 @Transactional
 public Respuestas.Acceso registrar(Peticiones.Registro p){
  return emitir(alta(p,Rol.CLIENTE));
 }
 @Transactional
 public Respuestas.Perfil crearPersonal(Peticiones.Registro p){return Respuestas.Perfil.de(alta(p,Rol.ADMIN));}
 private Usuario alta(Peticiones.Registro p,Rol rol){
  String email=normaliza(p.email());
  if(usuarios.findByEmailIgnoreCase(email).isPresent()) throw new PortalExcepcion(HttpStatus.CONFLICT,"El correo ya está registrado");
  Usuario u=new Usuario();u.nombres=p.nombres().strip();u.apellidos=p.apellidos().strip();u.email=email;
  u.telefono=p.telefono();u.claveHash=encoder.encode(p.clave());u.rol=rol;
  return usuarios.saveAndFlush(u);
 }
 @Transactional
 public Respuestas.Acceso ingresar(Peticiones.Acceso p,Rol esperado){
  Usuario u=usuarios.findByEmailIgnoreCase(normaliza(p.email())).orElseThrow(()->new PortalExcepcion(HttpStatus.UNAUTHORIZED,"Credenciales incorrectas"));
  if(!u.activo||u.rol!=esperado||!encoder.matches(p.clave(),u.claveHash)) throw new PortalExcepcion(HttpStatus.UNAUTHORIZED,"Credenciales incorrectas para este portal");
  return emitir(u);
 }
private Respuestas.Acceso emitir(Usuario u) {

    Instant limite = Instant.now().plus(8, ChronoUnit.HOURS);

    String token = jwt.generar(u, limite);

    Sesion s = new Sesion();
    s.usuario = u;
    s.tokenHash = huella(token);
    s.expiraEn = limite;

    sesiones.save(s);

    return new Respuestas.Acceso(
        token,
        limite,
        Respuestas.Perfil.de(u)
    );
}
 @Transactional(readOnly = true)
public Optional<Usuario> porToken(String token) {

    return jwt.validar(token).flatMap(datos ->
        sesiones.findByTokenHashAndExpiraEnAfter(
            huella(token),
            Instant.now()
        )
        .map(s -> s.usuario)
        .filter(u ->
            u.activo &&
            u.id.toString().equals(datos.getSubject())
        )
    );
}

 @Transactional
 public void salir(String token){sesiones.deleteByTokenHash(huella(token));}
}
