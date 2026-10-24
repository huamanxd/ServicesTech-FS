package pe.fullstack.servicesbackend.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.fullstack.servicesbackend.config.OpenApi;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas.Perfil;
import pe.fullstack.servicesbackend.modelo.Rol;
import pe.fullstack.servicesbackend.modelo.Usuario;
import pe.fullstack.servicesbackend.servicio.UsuarioServicio;

import java.util.List;

@Tag(name = "Administración: usuarios")
@SecurityRequirement(name = OpenApi.BEARER)
@RestController
@RequestMapping("/api/admin/usuarios")
@RequiredArgsConstructor
public class AdminUsuarioControlador {

    private final UsuarioServicio usuarios;

    @Operation(summary = "Usuarios (opcional ?rol=CLIENTE|TECNICO|ADMINISTRADOR)")
    @GetMapping
    public List<Perfil> listar(@RequestParam(required = false) Rol.Codigo rol) {
        return usuarios.listar(rol);
    }

    @GetMapping("/{id}")
    public Perfil obtener(@PathVariable Long id) {
        return usuarios.obtener(id);
    }

    @Operation(summary = "Crear técnico o administrador (solo un administrador puede hacerlo)")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Perfil crear(@Valid @RequestBody Peticiones.NuevoPersonal p) {
        return usuarios.crearPersonal(p);
    }

    @Operation(summary = "Activar o desactivar un usuario (no se eliminan usuarios)")
    @PatchMapping("/{id}/estado")
    public Perfil cambiarActivo(@AuthenticationPrincipal Usuario u, @PathVariable Long id,
                                @Valid @RequestBody Peticiones.CambioActivo p) {
        return usuarios.cambiarActivo(id, p.activo(), u.getId());
    }
}
