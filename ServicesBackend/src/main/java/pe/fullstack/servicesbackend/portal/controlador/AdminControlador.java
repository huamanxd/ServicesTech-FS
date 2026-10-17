package pe.fullstack.servicesbackend.portal.controlador;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import pe.fullstack.servicesbackend.portal.dto.*;
import pe.fullstack.servicesbackend.portal.modelo.*;
import pe.fullstack.servicesbackend.portal.repositorio.*;
import pe.fullstack.servicesbackend.portal.servicio.*;
import java.util.*;
@RestController @RequestMapping("/api/admin")
public class AdminControlador {
 private final ServicioRepositorio servicios;private final SolicitudRepositorio solicitudes;private final IdentidadServicio identidad;
 public AdminControlador(ServicioRepositorio servicios,SolicitudRepositorio solicitudes,IdentidadServicio identidad){this.servicios=servicios;this.solicitudes=solicitudes;this.identidad=identidad;}
 @GetMapping("/servicios") public List<Respuestas.ServicioVista> todos(){return servicios.findAllByOrderByIdAsc().stream().map(Respuestas.ServicioVista::de).toList();}
 @PostMapping("/servicios") @ResponseStatus(HttpStatus.CREATED) @Transactional
 public Respuestas.ServicioVista agregar(@Valid @RequestBody Peticiones.ServicioForm f){
  if(servicios.existsByCodigoIgnoreCase(f.codigo())) throw new PortalExcepcion(HttpStatus.CONFLICT,"Código ya registrado");
  Servicio s=new Servicio();copiar(s,f);return Respuestas.ServicioVista.de(servicios.save(s));
 }
 @PutMapping("/servicios/{id}") @Transactional
 public Respuestas.ServicioVista editar(@PathVariable Long id,@Valid @RequestBody Peticiones.ServicioForm f){
  Servicio s=servicios.findById(id).orElseThrow(()->new PortalExcepcion(HttpStatus.NOT_FOUND,"Servicio no encontrado"));
  if(!s.codigo.equalsIgnoreCase(f.codigo())&&servicios.existsByCodigoIgnoreCase(f.codigo())) throw new PortalExcepcion(HttpStatus.CONFLICT,"Código ya registrado");
  copiar(s,f);return Respuestas.ServicioVista.de(servicios.save(s));
 }
 @DeleteMapping("/servicios/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
 public void desactivar(@PathVariable Long id){Servicio s=servicios.findById(id).orElseThrow(()->new PortalExcepcion(HttpStatus.NOT_FOUND,"Servicio no encontrado"));s.activo=false;servicios.save(s);}
 private static void copiar(Servicio s,Peticiones.ServicioForm f){s.codigo=f.codigo();s.nombre=f.nombre().strip();s.descripcion=f.descripcion().strip();s.categoria=f.categoria();s.precioReferencial=f.precioReferencial();s.duracionMinutos=f.duracionMinutos();s.activo=f.activo();}
 @GetMapping("/solicitudes") public List<Respuestas.SolicitudVista> solicitudes(){return solicitudes.findAllByOrderByCreadoEnDesc().stream().map(Respuestas.SolicitudVista::de).toList();}
 @PatchMapping("/solicitudes/{id}/estado") @Transactional
 public Respuestas.SolicitudVista estado(@PathVariable Long id,@Valid @RequestBody Peticiones.CambiarEstado p){
  Solicitud s=solicitudes.findById(id).orElseThrow(()->new PortalExcepcion(HttpStatus.NOT_FOUND,"Solicitud no encontrada"));
  try{s.estado=EstadoSolicitud.valueOf(p.estado());}catch(IllegalArgumentException e){throw new PortalExcepcion(HttpStatus.BAD_REQUEST,"Estado no válido");}
  return Respuestas.SolicitudVista.de(solicitudes.save(s));
 }
 @PostMapping("/personal") @ResponseStatus(HttpStatus.CREATED)
 public Respuestas.Perfil nuevoPersonal(@Valid @RequestBody Peticiones.Registro f){return identidad.crearPersonal(f);}
}
