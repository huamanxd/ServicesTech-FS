package pe.fullstack.servicesbackend.portal.servicio;
import org.springframework.http.HttpStatus;
public class PortalExcepcion extends RuntimeException {
 public final HttpStatus estado;
 public PortalExcepcion(HttpStatus estado,String mensaje){super(mensaje);this.estado=estado;}
}
