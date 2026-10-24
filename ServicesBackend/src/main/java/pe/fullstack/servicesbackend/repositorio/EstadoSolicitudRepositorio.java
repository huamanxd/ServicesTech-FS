package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.modelo.EstadoSolicitud;

import java.util.Optional;

public interface EstadoSolicitudRepositorio extends JpaRepository<EstadoSolicitud, Long> {
    Optional<EstadoSolicitud> findByNombre(EstadoSolicitud.Codigo nombre);
}
