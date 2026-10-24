package pe.fullstack.servicesbackend.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.fullstack.servicesbackend.config.OpenApi;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas.ServicioVista;
import pe.fullstack.servicesbackend.servicio.CatalogoServicio;

import java.util.List;

@Tag(name = "Administración: catálogo de servicios")
@SecurityRequirement(name = OpenApi.BEARER)
@RestController
@RequestMapping("/api/admin/servicios")
@RequiredArgsConstructor
public class AdminCatalogoControlador {

    private final CatalogoServicio catalogo;

    @Operation(summary = "Todos los servicios, incluidos los inactivos")
    @GetMapping
    public List<ServicioVista> todos() {
        return catalogo.todosLosServicios();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioVista crear(@Valid @RequestBody Peticiones.ServicioForm f) {
        return catalogo.crearServicio(f);
    }

    @PutMapping("/{id}")
    public ServicioVista editar(@PathVariable Long id, @Valid @RequestBody Peticiones.ServicioForm f) {
        return catalogo.editarServicio(id, f);
    }

    @Operation(summary = "Baja lógica (activo = false); el historial se conserva")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desactivar(@PathVariable Long id) {
        catalogo.desactivarServicio(id);
    }
}
