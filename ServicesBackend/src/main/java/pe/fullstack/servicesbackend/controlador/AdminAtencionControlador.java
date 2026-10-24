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
import pe.fullstack.servicesbackend.dto.Respuestas.*;
import pe.fullstack.servicesbackend.modelo.EstadoOrden;
import pe.fullstack.servicesbackend.modelo.EstadoSolicitud;
import pe.fullstack.servicesbackend.modelo.Usuario;
import pe.fullstack.servicesbackend.servicio.DashboardServicio;
import pe.fullstack.servicesbackend.servicio.OrdenServicio;
import pe.fullstack.servicesbackend.servicio.SolicitudServicio;

import java.util.List;

@Tag(name = "Administración: solicitudes, órdenes y dashboard")
@SecurityRequirement(name = OpenApi.BEARER)
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAtencionControlador {

    private final SolicitudServicio solicitudes;
    private final OrdenServicio ordenes;
    private final DashboardServicio dashboard;

    @Operation(summary = "Dashboard: órdenes, retrasos, tiempos, rendimiento técnico, cumplimiento y satisfacción")
    @GetMapping("/dashboard")
    public DashboardAdmin dashboard() {
        return dashboard.administrador();
    }

    @Operation(summary = "Solicitudes (opcional ?estado=PENDIENTE|EN_REVISION|APROBADA|RECHAZADA|CANCELADA)")
    @GetMapping("/solicitudes")
    public List<SolicitudVista> solicitudes(@RequestParam(required = false) EstadoSolicitud.Codigo estado) {
        return solicitudes.todas(estado);
    }

    @Operation(summary = "Revisar una solicitud: EN_REVISION, APROBADA o RECHAZADA")
    @PostMapping("/solicitudes/{id}/revision")
    public SolicitudVista revisar(@PathVariable Long id, @Valid @RequestBody Peticiones.Revision p) {
        return solicitudes.revisar(id, p);
    }

    @Operation(summary = "Convertir una solicitud aprobada en orden (opcionalmente asignando técnico)")
    @PostMapping("/solicitudes/{id}/orden")
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenVista generarOrden(@AuthenticationPrincipal Usuario u, @PathVariable Long id,
                                   @Valid @RequestBody Peticiones.NuevaOrden p) {
        return ordenes.generar(id, p, u.getId());
    }

    @Operation(summary = "Órdenes (opcional ?estado=PENDIENTE|ASIGNADA|EN_ATENCION|RESUELTA|CERRADA|CANCELADA)")
    @GetMapping("/ordenes")
    public List<OrdenVista> ordenes(@RequestParam(required = false) EstadoOrden.Codigo estado) {
        return ordenes.todas(estado);
    }

    @Operation(summary = "Asignar o reasignar un técnico")
    @PostMapping("/ordenes/{id}/asignar")
    public OrdenVista asignar(@AuthenticationPrincipal Usuario u, @PathVariable Long id,
                              @Valid @RequestBody Peticiones.Asignacion p) {
        return ordenes.asignar(id, p, u.getId());
    }

    @Operation(summary = "Cerrar una orden resuelta")
    @PostMapping("/ordenes/{id}/cerrar")
    public OrdenVista cerrar(@AuthenticationPrincipal Usuario u, @PathVariable Long id,
                             @Valid @RequestBody(required = false) Peticiones.Observacion p) {
        return ordenes.cerrar(id, p == null ? null : p.observacion(), u.getId());
    }

    @Operation(summary = "Cancelar una orden no resuelta (motivo obligatorio)")
    @PostMapping("/ordenes/{id}/cancelar")
    public OrdenVista cancelar(@AuthenticationPrincipal Usuario u, @PathVariable Long id,
                               @Valid @RequestBody Peticiones.Cancelacion p) {
        return ordenes.cancelar(id, p.observacion(), u.getId());
    }

    @Operation(summary = "Bitácora de cambios de estado de una orden")
    @GetMapping("/ordenes/{id}/historial")
    public List<HistorialVista> historial(@PathVariable Long id) {
        return ordenes.historial(id);
    }
}
