package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.modelo.EstadoSolicitud;
import pe.fullstack.servicesbackend.modelo.Solicitud;

import java.util.List;

public interface SolicitudRepositorio extends JpaRepository<Solicitud, Long> {

    @EntityGraph(attributePaths = {"estado", "prioridad", "servicio", "cliente"})
    List<Solicitud> findByClienteIdOrderByFechaSolicitudDesc(Long clienteId);

    @EntityGraph(attributePaths = {"estado", "prioridad", "servicio", "cliente"})
    List<Solicitud> findAllByOrderByFechaSolicitudDesc();

    @EntityGraph(attributePaths = {"estado", "prioridad", "servicio", "cliente"})
    List<Solicitud> findByEstadoNombreOrderByFechaSolicitudDesc(EstadoSolicitud.Codigo estado);
}
