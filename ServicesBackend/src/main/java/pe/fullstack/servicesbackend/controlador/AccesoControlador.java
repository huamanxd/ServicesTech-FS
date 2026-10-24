package pe.fullstack.servicesbackend.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.fullstack.servicesbackend.config.FiltroToken;
import pe.fullstack.servicesbackend.config.OpenApi;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas;
import pe.fullstack.servicesbackend.dto.VistaMapper;
import pe.fullstack.servicesbackend.modelo.Usuario;
import pe.fullstack.servicesbackend.servicio.IdentidadServicio;
import pe.fullstack.servicesbackend.servicio.IdentidadServicio.Portal;

import java.util.Map;

@Tag(name = "Acceso", description = "Registro de clientes, inicio y cierre de sesión")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccesoControlador {

    private final IdentidadServicio identidad;
    private final VistaMapper mapper;

    @Operation(summary = "Registro público de clientes (siempre rol CLIENTE)")
    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    public Respuestas.Acceso registro(@Valid @RequestBody Peticiones.Registro p) {
        return identidad.registrarCliente(p);
    }

    @Operation(summary = "Inicio de sesión del Portal Cliente")
    @PostMapping("/cliente")
    public Respuestas.Acceso cliente(@Valid @RequestBody Peticiones.Acceso p) {
        return identidad.ingresar(p, Portal.CLIENTE);
    }

    @Operation(summary = "Inicio de sesión de personal (técnicos y administradores)")
    @PostMapping("/personal")
    public Respuestas.Acceso personal(@Valid @RequestBody Peticiones.Acceso p) {
        return identidad.ingresar(p, Portal.PERSONAL);
    }

    @Operation(summary = "Perfil de la sesión actual", security = @SecurityRequirement(name = OpenApi.BEARER))
    @GetMapping("/perfil")
    public Respuestas.Perfil perfil(@AuthenticationPrincipal Usuario u) {
        return mapper.perfil(u);
    }

    @Operation(summary = "Cierra la sesión (revoca el token)", security = @SecurityRequirement(name = OpenApi.BEARER))
    @PostMapping("/salir")
    public Map<String, String> salir(HttpServletRequest request) {
        identidad.salir(FiltroToken.token(request));
        return Map.of("message", "Sesión cerrada");
    }
}
