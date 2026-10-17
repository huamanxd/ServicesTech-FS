package pe.fullstack.servicesbackend.portal.controlador;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import pe.fullstack.servicesbackend.portal.modelo.*;
import pe.fullstack.servicesbackend.portal.dto.*;
import pe.fullstack.servicesbackend.portal.servicio.IdentidadServicio;
import java.util.Map;
@RestController @RequestMapping("/api/auth")
public class AccesoControlador {
 private final IdentidadServicio identidad;
 public AccesoControlador(IdentidadServicio identidad){this.identidad=identidad;}
 @PostMapping("/registro") @ResponseStatus(HttpStatus.CREATED)
 public Respuestas.Acceso registro(@Valid @RequestBody Peticiones.Registro p){return identidad.registrar(p);}
 @PostMapping("/cliente") public Respuestas.Acceso cliente(@Valid @RequestBody Peticiones.Acceso p){return identidad.ingresar(p,Rol.CLIENTE);}
 @PostMapping("/personal") public Respuestas.Acceso personal(@Valid @RequestBody Peticiones.Acceso p){return identidad.ingresar(p,Rol.ADMIN);}
 @GetMapping("/perfil") public Respuestas.Perfil perfil(@AuthenticationPrincipal Usuario u){return Respuestas.Perfil.de(u);}
 @PostMapping("/salir") public Map<String,String> salir(HttpServletRequest req){identidad.salir(req.getHeader("Authorization").substring(7).trim());return Map.of("message","Sesión cerrada");}
}
