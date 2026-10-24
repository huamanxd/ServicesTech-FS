package pe.fullstack.servicesbackend.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/** Cuerpos de las peticiones REST, con sus validaciones. */
public final class Peticiones {

    private Peticiones() {
    }

    private static final String DNI = "^[0-9]{8}$";
    private static final String CELULAR = "^9[0-9]{8}$";

    public record Registro(
            @NotBlank @Size(min = 2, max = 90) String nombres,
            @NotBlank @Size(min = 2, max = 90) String apellidos,
            @NotBlank @Pattern(regexp = DNI, message = "El DNI debe tener 8 dígitos") String dni,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Pattern(regexp = CELULAR, message = "Debe tener 9 dígitos y empezar con 9") String telefono,
            @NotBlank @Size(min = 10, max = 72, message = "La contraseña debe tener entre 10 y 72 caracteres") String clave) {
    }

    /** Alta de personal por un administrador: rol TECNICO o ADMINISTRADOR. */
    public record NuevoPersonal(
            @NotBlank @Size(min = 2, max = 90) String nombres,
            @NotBlank @Size(min = 2, max = 90) String apellidos,
            @NotBlank @Pattern(regexp = DNI, message = "El DNI debe tener 8 dígitos") String dni,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Pattern(regexp = CELULAR, message = "Debe tener 9 dígitos y empezar con 9") String telefono,
            @NotBlank @Size(min = 10, max = 72, message = "La contraseña debe tener entre 10 y 72 caracteres") String clave,
            @NotBlank @Pattern(regexp = "TECNICO|ADMINISTRADOR", message = "Rol permitido: TECNICO o ADMINISTRADOR") String rol) {
    }

    public record Acceso(@NotBlank @Email String email, @NotBlank String clave) {
    }

    public record CambioActivo(@NotNull Boolean activo) {
    }

    public record ServicioForm(
            @NotBlank @Pattern(regexp = "[A-Z0-9-]{3,45}", message = "Usa de 3 a 45 caracteres: A-Z, 0-9 o guion") String codigo,
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Size(max = 600) String descripcion,
            @NotBlank @Pattern(regexp = "EQUIPOS|SOFTWARE|REDES|SEGURIDAD|EMPRESAS", message = "Categoría no válida") String categoria,
            @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal precioReferencial,
            @NotNull @Min(15) @Max(10080) Integer duracionMinutos,
            @NotNull Boolean activo) {
    }

    public record NuevaSolicitud(
            @NotNull Long servicioId,
            @NotNull Long prioridadId,
            @NotBlank @Size(min = 5, max = 150) String asunto,
            @NotBlank @Size(min = 10, max = 700) String descripcion,
            @Size(max = 220) String direccion) {
    }

    /** Decisión del administrador sobre una solicitud. */
    public record Revision(
            @NotBlank @Pattern(regexp = "EN_REVISION|APROBADA|RECHAZADA", message = "Estado permitido: EN_REVISION, APROBADA o RECHAZADA") String estado) {
    }

    /** Conversión de una solicitud aprobada en orden. Si no se indican servicios, se usa el solicitado. */
    public record NuevaOrden(
            List<@NotNull Long> servicioIds,
            @Future(message = "La fecha límite debe ser futura") Instant fechaLimite,
            @Size(max = 200) String observaciones,
            Long tecnicoId) {
    }

    public record Asignacion(@NotNull Long tecnicoId, @Size(max = 200) String observacion) {
    }

    public record Observacion(@Size(max = 200) String observacion) {
    }

    public record Cancelacion(@NotBlank @Size(max = 200) String observacion) {
    }

    public record Solucion(@NotBlank @Size(min = 10, max = 500) String descripcion) {
    }

    public record Calificacion(@NotNull @Min(1) @Max(5) Integer calificacion, @Size(max = 200) String comentario) {
    }
}
