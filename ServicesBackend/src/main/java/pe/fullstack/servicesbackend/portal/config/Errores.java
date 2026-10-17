package pe.fullstack.servicesbackend.portal.config;
import org.springframework.http.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pe.fullstack.servicesbackend.portal.servicio.PortalExcepcion;
import java.util.*;
@RestControllerAdvice(basePackages="pe.fullstack.servicesbackend.portal")
public class Errores {
 @ExceptionHandler(PortalExcepcion.class)
 public ResponseEntity<?> dominio(PortalExcepcion ex){return ResponseEntity.status(ex.estado).body(Map.of("message",ex.getMessage()));}
 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<?> validacion(MethodArgumentNotValidException ex){
  Map<String,String> campos=new LinkedHashMap<>();ex.getBindingResult().getFieldErrors().forEach(e->campos.putIfAbsent(e.getField(),e.getDefaultMessage()));
  return ResponseEntity.badRequest().body(Map.of("message","Revisa los campos del formulario","fields",campos));
 }
 @ExceptionHandler(DataIntegrityViolationException.class)
 public ResponseEntity<?> duplicado(DataIntegrityViolationException ex){return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","El registro ya existe o está siendo utilizado"));}
 @ExceptionHandler(Exception.class)
 public ResponseEntity<?> interno(Exception ex){return ResponseEntity.status(500).body(Map.of("message","Ocurrió un error interno. Inténtalo nuevamente"));}
}
