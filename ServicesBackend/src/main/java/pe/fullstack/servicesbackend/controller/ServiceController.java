package pe.fullstack.servicesbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.fullstack.servicesbackend.portal.dto.Respuestas;
import pe.fullstack.servicesbackend.portal.repositorio.ServicioRepositorio;
import java.util.List;

/** Controlador original completado con los servicios persistentes en PostgreSQL. */
@RestController
@RequestMapping("/api/servicios")
public class ServiceController {
    private final ServicioRepositorio repositorio;
    public ServiceController(ServicioRepositorio repositorio) { this.repositorio = repositorio; }
    @GetMapping
    public List<Respuestas.ServicioVista> listar() {
        return repositorio.findByActivoTrueOrderByIdAsc().stream().map(Respuestas.ServicioVista::de).toList();
    }
}
