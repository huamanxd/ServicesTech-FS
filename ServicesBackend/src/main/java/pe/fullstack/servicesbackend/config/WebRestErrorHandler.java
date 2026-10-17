package pe.fullstack.servicesbackend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.fullstack.servicesbackend.portal.servicio.PortalExcepcion;
import java.util.List;

/** Adaptación segura del handler original, solo para controladores originales. */
@RestControllerAdvice(basePackages = "pe.fullstack.servicesbackend.controller")
public class WebRestErrorHandler {
    @ExceptionHandler(PortalExcepcion.class)
    public ResponseEntity<ExceptionResponse> handlePortal(PortalExcepcion ex) {
        return ResponseEntity.status(ex.estado).body(ExceptionResponse.builder()
                .code("PORTAL_ERROR").message(ex.getMessage()).validations(List.of()).build());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ExceptionResponse> handleFormError(MethodArgumentNotValidException ex) {
        var errores = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> ValidationResponse.builder().field(e.getField()).message(e.getDefaultMessage()).build())
                .toList();
        return ResponseEntity.badRequest().body(ExceptionResponse.builder()
                .code("VALIDATION_ERROR").message("Revisa los datos del formulario")
                .validations(errores).build());
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ExceptionResponse.builder()
                .code("INTERNAL_ERROR").message("Error interno del servidor")
                .validations(List.of()).build());
    }
}
