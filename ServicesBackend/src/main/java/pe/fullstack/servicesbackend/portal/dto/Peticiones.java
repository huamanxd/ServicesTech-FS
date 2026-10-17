package pe.fullstack.servicesbackend.portal.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public final class Peticiones {
 private Peticiones() {}
 public record Registro(
  @NotBlank @Size(min=2,max=90) String nombres,
  @NotBlank @Size(min=2,max=90) String apellidos,
  @NotBlank @Email @Size(max=180) String email,
  @Pattern(regexp="^$|^9[0-9]{8}$",message="Debe tener 9 dígitos y empezar con 9") String telefono,
  @NotBlank @Size(min=10,max=72) String clave) {}
 public record Acceso(@NotBlank @Email String email,@NotBlank String clave) {}
 public record ServicioForm(
  @NotBlank @Pattern(regexp="[A-Z0-9-]{3,45}") String codigo,
  @NotBlank @Size(max=120) String nombre,
  @NotBlank @Size(max=600) String descripcion,
  @NotBlank @Pattern(regexp="EQUIPOS|SOFTWARE|REDES|SEGURIDAD|EMPRESAS") String categoria,
  @NotNull @DecimalMin("0.00") @Digits(integer=8,fraction=2) BigDecimal precioReferencial,
  @NotNull @Min(15) @Max(10080) Integer duracionMinutos,
  @NotNull Boolean activo) {}
 public record SolicitudForm(@NotNull Long servicioId,@NotBlank @Size(min=10,max=700) String detalle,
  @Size(max=220) String direccion) {}
 public record CambiarEstado(@NotNull String estado) {}
}
