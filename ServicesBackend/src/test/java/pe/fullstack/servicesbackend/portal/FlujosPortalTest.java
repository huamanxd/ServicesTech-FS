package pe.fullstack.servicesbackend.portal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import pe.fullstack.servicesbackend.portal.dto.Peticiones;
import pe.fullstack.servicesbackend.portal.modelo.Rol;
import pe.fullstack.servicesbackend.portal.modelo.Usuario;
import pe.fullstack.servicesbackend.portal.repositorio.SesionRepositorio;
import pe.fullstack.servicesbackend.portal.repositorio.UsuarioRepositorio;
import pe.fullstack.servicesbackend.portal.servicio.IdentidadServicio;
import pe.fullstack.servicesbackend.portal.servicio.PortalExcepcion;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Pruebas sin conexión a PostgreSQL: las consultas se simulan con Mockito. */
class FlujosPortalTest {
 private UsuarioRepositorio usuarios;
 private SesionRepositorio sesiones;
 private IdentidadServicio identidad;
 private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(4);

 @BeforeEach void preparar() {
  usuarios=mock(UsuarioRepositorio.class);
  sesiones=mock(SesionRepositorio.class);
  identidad=new IdentidadServicio(usuarios,sesiones,encoder);
 }

 private Peticiones.Registro registro() {
  return new Peticiones.Registro("Ana", "Rojas", "ANA@Ejemplo.com", "987654321", "ClaveRobusta!2026");
 }

 @Test void registroPublicoSiempreEsClienteYPersistenciaCifraClave() {
  when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> {
   Usuario u=inv.getArgument(0);u.id=1L;return u;
  });
  var acceso=identidad.registrar(registro());
  var guardado=org.mockito.ArgumentCaptor.forClass(Usuario.class);
  verify(usuarios).saveAndFlush(guardado.capture());
  assertEquals(Rol.CLIENTE, guardado.getValue().rol);
  assertEquals("ana@ejemplo.com", guardado.getValue().email);
  assertNotEquals(registro().clave(), guardado.getValue().claveHash);
  assertTrue(encoder.matches(registro().clave(), guardado.getValue().claveHash));
  assertEquals("CLIENTE", acceso.usuario().rol());
  assertNotNull(acceso.token());
  verify(sesiones).save(any());
 }

 @Test void duplicadoProduceConflictoSinCrearSesion() {
  Usuario existente=new Usuario();existente.id=5L;
  when(usuarios.findByEmailIgnoreCase("ana@ejemplo.com")).thenReturn(Optional.of(existente));
  var ex=assertThrows(PortalExcepcion.class,()->identidad.registrar(registro()));
  assertEquals(HttpStatus.CONFLICT,ex.estado);
  verify(usuarios,never()).saveAndFlush(any());
  verifyNoInteractions(sesiones);
 }

 @Test void clienteNoPuedeEntrarComoAdministrador() {
  Usuario cliente=new Usuario();cliente.id=7L;cliente.email="ana@ejemplo.com";
  cliente.rol=Rol.CLIENTE;cliente.activo=true;cliente.claveHash=encoder.encode("ClaveRobusta!2026");
  when(usuarios.findByEmailIgnoreCase("ana@ejemplo.com")).thenReturn(Optional.of(cliente));
  var ex=assertThrows(PortalExcepcion.class,()->identidad.ingresar(
   new Peticiones.Acceso("Ana@Ejemplo.com", "ClaveRobusta!2026"),Rol.ADMIN));
  assertEquals(HttpStatus.UNAUTHORIZED,ex.estado);
  verifyNoInteractions(sesiones);
 }

 @Test void altaDelPersonalRequiereAdminExternoYNoGeneraSesionNueva() {
  when(usuarios.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> {
   Usuario u=inv.getArgument(0);u.id=11L;return u;
  });
  var creado=identidad.crearPersonal(registro());
  assertEquals("ADMIN",creado.rol());
  verify(usuarios).saveAndFlush(argThat(u->u.rol==Rol.ADMIN));
  verifyNoInteractions(sesiones);
 }
}
