package pe.fullstack.servicesbackend.portal;

import org.junit.jupiter.api.Test;
import pe.fullstack.servicesbackend.portal.servicio.IdentidadServicio;
import static org.junit.jupiter.api.Assertions.*;

class IdentidadServicioTest {
 @Test void normalizaCorreo() {
  assertEquals("ana@ejemplo.com", IdentidadServicio.normaliza("  ANA@Ejemplo.com  "));
 }
 @Test void huellaEsHashYEsDeterminista() {
  String original="mi-token-de-prueba";
  String hash=IdentidadServicio.huella(original);
  assertEquals(64, hash.length());
  assertNotEquals(original, hash);
  assertEquals(hash, IdentidadServicio.huella(original));
  assertNotEquals(hash, IdentidadServicio.huella("otro-token"));
 }
}
