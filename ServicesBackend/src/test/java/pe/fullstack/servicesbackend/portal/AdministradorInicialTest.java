package pe.fullstack.servicesbackend.portal;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import pe.fullstack.servicesbackend.portal.config.AdministradorInicial;
import pe.fullstack.servicesbackend.portal.modelo.Rol;
import pe.fullstack.servicesbackend.portal.modelo.Usuario;
import pe.fullstack.servicesbackend.portal.repositorio.UsuarioRepositorio;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AdministradorInicialTest {
 @Test void creaCuentaAdminSolicitadaConClaveHasheada() throws Exception {
  var repo=mock(UsuarioRepositorio.class);
  var encoder=new BCryptPasswordEncoder(4);
  var inicial=new AdministradorInicial(repo, encoder);
  ReflectionTestUtils.setField(inicial,"email","admin@admin.com");
  ReflectionTestUtils.setField(inicial,"clave","admin123456789");
  inicial.run();
  var captor=org.mockito.ArgumentCaptor.forClass(Usuario.class);
  verify(repo).save(captor.capture());
  var user=captor.getValue();
  assertEquals("admin@admin.com",user.email);
  assertEquals(Rol.ADMIN,user.rol);
  assertTrue(encoder.matches("admin123456789",user.claveHash));
  assertNotEquals("admin123456789",user.claveHash);
 }
 @Test void noReemplazaUsuarioYaExistente() throws Exception {
  var repo=mock(UsuarioRepositorio.class);
  var inicial=new AdministradorInicial(repo,new BCryptPasswordEncoder(4));
  ReflectionTestUtils.setField(inicial,"email","admin@admin.com");
  ReflectionTestUtils.setField(inicial,"clave","admin123456789");
  when(repo.findByEmailIgnoreCase("admin@admin.com")).thenReturn(Optional.of(new Usuario()));
  inicial.run();
  verify(repo,never()).save(any());
 }
}
