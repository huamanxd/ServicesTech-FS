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
import pe.fullstack.servicesbackend.modelo.Usuario;
import pe.fullstack.servicesbackend.servicio.DashboardServicio;
import pe.fullstack.servicesbackend.servicio.OrdenServicio;
import pe.fullstack.servicesbackend.servicio.SolicitudServicio;

import java.util.List;

/** Todo lo que ve el cliente es suyo: el id sale del token, nunca del cuerpo ni de la URL. */
@Tag(name = "Cliente")
@SecurityRequirement(name = OpenApi.BEARER)
@RestController
@RequestMapping("/api/cliente")
@RequiredArgsConstructor
public class ClienteControlador {

    private final SolicitudServicio solicitudes;
    private final OrdenServicio ordenes;
    private final DashboardServicio dashboard;

    @Operation(summary = "Indicadores del cliente: estados, tiempos, cumplimiento y satisfacción")
    @GetMapping("/dashboard")
    public DashboardCliente dashboard(@AuthenticationPrincipal Usuario u) {
        return dashboard.cliente(u.getId());
    }

    @Operation(summary = "Historial de solicitudes propias, con su orden si existe")
    @GetMapping("/solicitudes")
    public List<SolicitudVista> mias(@AuthenticationPrincipal Usuario u) {
        return solicitudes.delCliente(u.getId());
    }

    @Operation(summary = "Registra una solicitud (queda PENDIENTE)")
    @PostMapping("/solicitudes")
    @ResponseStatus(HttpStatus.CREATED)
    public SolicitudVista registrar(@AuthenticationPrincipal Usuario u, @Valid @RequestBody Peticiones.NuevaSolicitud p) {
        return solicitudes.registrar(u.getId(), p);
    }

    @Operation(summary = "Cancela una solicitud propia que aún no tiene orden")
    @PostMapping("/solicitudes/{id}/cancelar")
    public SolicitudVista cancelar(@AuthenticationPrincipal Usuario u, @PathVariable Long id) {
        return solicitudes.cancelarPorCliente(u.getId(), id);
    }

    @Operation(summary = "Seguimiento: historial de estados de una orden propia")
    @GetMapping("/ordenes/{id}/historial")
    public List<HistorialVista> historial(@AuthenticationPrincipal Usuario u, @PathVariable Long id) {
        return ordenes.historialDelCliente(id, u.getId());
    }

    @Operation(summary = "Califica (1-5) un servicio resuelto; la orden queda CERRADA")
    @PostMapping("/ordenes/{id}/satisfaccion")
    public OrdenVista calificar(@AuthenticationPrincipal Usuario u, @PathVariable Long id,
                                @Valid @RequestBody Peticiones.Calificacion p) {
        return ordenes.calificar(id, u.getId(), p);
    }
}
