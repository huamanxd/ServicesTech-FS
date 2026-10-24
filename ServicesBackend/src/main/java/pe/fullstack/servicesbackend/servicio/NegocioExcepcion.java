package pe.fullstack.servicesbackend.servicio;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Error de negocio con el código HTTP que debe recibir el cliente. */
@Getter
public class NegocioExcepcion extends RuntimeException {

    private final HttpStatus estado;

    public NegocioExcepcion(HttpStatus estado, String mensaje) {
        super(mensaje);
        this.estado = estado;
    }

    public static NegocioExcepcion noEncontrado(String mensaje) {
        return new NegocioExcepcion(HttpStatus.NOT_FOUND, mensaje);
    }

    public static NegocioExcepcion conflicto(String mensaje) {
        return new NegocioExcepcion(HttpStatus.CONFLICT, mensaje);
    }

    public static NegocioExcepcion prohibido(String mensaje) {
        return new NegocioExcepcion(HttpStatus.FORBIDDEN, mensaje);
    }

    public static NegocioExcepcion invalido(String mensaje) {
        return new NegocioExcepcion(HttpStatus.BAD_REQUEST, mensaje);
    }
}
