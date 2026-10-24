package pe.fullstack.servicesbackend.controlador;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.fullstack.servicesbackend.dto.Respuestas;
import pe.fullstack.servicesbackend.servicio.CatalogoServicio;

import java.util.List;

@Tag(name = "Catálogo público")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CatalogoControlador {

    private final CatalogoServicio catalogo;

    @Operation(summary = "Servicios activos con precio referencial en soles")
    @GetMapping("/servicios")
    public List<Respuestas.ServicioVista> servicios() {
        return catalogo.serviciosActivos();
    }

    @Operation(summary = "Prioridades y horas objetivo de atención")
    @GetMapping("/prioridades")
    public List<Respuestas.PrioridadVista> prioridades() {
        return catalogo.prioridades();
    }
}
