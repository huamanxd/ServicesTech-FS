package pe.fullstack.servicesbackend.portal.dto;
import java.math.BigDecimal;
import java.time.Instant;
import pe.fullstack.servicesbackend.portal.modelo.*;
public final class Respuestas {
 private Respuestas() {}
 public record Perfil(Long id,String nombres,String apellidos,String email,String rol) {
  public static Perfil de(Usuario u){return new Perfil(u.id,u.nombres,u.apellidos,u.email,u.rol.name());}
 }
 public record Acceso(String token,Instant expiraEn,Perfil usuario) {}
 public record ServicioVista(Long id,String codigo,String nombre,String descripcion,String categoria,BigDecimal precioReferencial,int duracionMinutos,boolean activo) {
  public static ServicioVista de(Servicio s){return new ServicioVista(s.id,s.codigo,s.nombre,s.descripcion,s.categoria,s.precioReferencial,s.duracionMinutos,s.activo);}
 }
 public record SolicitudVista(Long id,Long clienteId,String cliente,String servicio,BigDecimal precioReferencial,String detalle,String direccion,String estado,Instant creadoEn) {
  public static SolicitudVista de(Solicitud s){return new SolicitudVista(s.id,s.cliente.id,s.cliente.nombres+" "+s.cliente.apellidos,s.servicio.nombre,s.precioReferencial,s.detalle,s.direccion,s.estado.name(),s.creadoEn);}
 }
}
