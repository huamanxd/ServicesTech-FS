package pe.fullstack.servicesbackend.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pe.fullstack.servicesbackend.config.OpenApi;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas.*;
import pe.fullstack.servicesbackend.modelo.Usuario;
import pe.fullstack.servicesbackend.servicio.DashboardServicio;
import pe.fullstack.servicesbackend.servicio.OrdenServicio;

import java.util.List;

@Tag(name = "Técnico")
@SecurityRequirement(name = OpenApi.BEARER)
@RestController
@RequestMapping("/api/tecnico")
@RequiredArgsConstructor
public class TecnicoControlador {

    private final OrdenServicio ordenes;
    private final DashboardServicio dashboard;

    @Operation(summary = "Indicadores del técnico")
    @GetMapping("/dashboard")
    public DashboardTecnico dashboard(@AuthenticationPrincipal Usuario u) {
        return dashboard.tecnico(u.getId());
    }

    @Operation(summary = "Bandeja: órdenes generadas a partir de solicitudes aprobadas que aún no tienen técnico")
    @GetMapping("/ordenes/disponibles")
    public List<OrdenVista> disponibles() {
        return ordenes.disponibles();
    }

    @Operation(summary = "Órdenes que tengo o tuve asignadas")
    @GetMapping("/ordenes")
    public List<OrdenVista> mias(@AuthenticationPrincipal Usuario u) {
        return ordenes.delTecnico(u.getId());
    }

    @Operation(summary = "Tomar una orden de la bandeja (queda ASIGNADA a mí)")
    @PostMapping("/ordenes/{id}/tomar")
    public OrdenVista tomar(@AuthenticationPrincipal Usuario u, @PathVariable Long id) {
        return ordenes.tomar(id, u.getId());
    }

    @Operation(summary = "Iniciar la atención (EN_ATENCION)")
    @PostMapping("/ordenes/{id}/iniciar")
    public OrdenVista iniciar(@AuthenticationPrincipal Usuario u, @PathVariable Long id,
                              @Valid @RequestBody(required = false) Peticiones.Observacion p) {
        return ordenes.iniciarAtencion(id, u.getId(), p == null ? null : p.observacion());
    }

    @Operation(summary = "Registrar la solución aplicada (RESUELTA)")
    @PostMapping("/ordenes/{id}/solucion")
    public OrdenVista solucion(@AuthenticationPrincipal Usuario u, @PathVariable Long id,
                               @Valid @RequestBody Peticiones.Solucion p) {
        return ordenes.registrarSolucion(id, u.getId(), p.descripcion());
    }

    @Operation(summary = "Historial de estados de una orden")
    @GetMapping("/ordenes/{id}/historial")
    public List<HistorialVista> historial(@AuthenticationPrincipal Usuario u, @PathVariable Long id) {
        return ordenes.historialDelTecnico(id, u.getId());
    }
}
