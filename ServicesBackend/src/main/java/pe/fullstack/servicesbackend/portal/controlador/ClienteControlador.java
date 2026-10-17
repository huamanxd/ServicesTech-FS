package pe.fullstack.servicesbackend.portal.controlador;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.fullstack.servicesbackend.portal.dto.*;
import pe.fullstack.servicesbackend.portal.modelo.*;
import pe.fullstack.servicesbackend.portal.repositorio.*;
import pe.fullstack.servicesbackend.portal.servicio.PortalExcepcion;
import java.util.*;
@RestController @RequestMapping("/api/cliente/solicitudes")
public class ClienteControlador {
 private final SolicitudRepositorio solicitudes;private final ServicioRepositorio servicios;
 public ClienteControlador(SolicitudRepositorio solicitudes,ServicioRepositorio servicios){this.solicitudes=solicitudes;this.servicios=servicios;}
 @GetMapping @Transactional(readOnly=true)
 public List<Respuestas.SolicitudVista> mias(@AuthenticationPrincipal Usuario u){return solicitudes.findByClienteIdOrderByCreadoEnDesc(u.id).stream().map(Respuestas.SolicitudVista::de).toList();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) @Transactional
 public Respuestas.SolicitudVista pedir(@AuthenticationPrincipal Usuario u,@Valid @RequestBody Peticiones.SolicitudForm p){
  Servicio servicio=servicios.findById(p.servicioId()).filter(s->s.activo).orElseThrow(()->new PortalExcepcion(HttpStatus.NOT_FOUND,"Servicio no disponible"));
  Solicitud s=new Solicitud();s.cliente=u;s.servicio=servicio;s.detalle=p.detalle().strip();s.direccion=p.direccion();s.precioReferencial=servicio.precioReferencial;
  return Respuestas.SolicitudVista.de(solicitudes.save(s));
 }
}
