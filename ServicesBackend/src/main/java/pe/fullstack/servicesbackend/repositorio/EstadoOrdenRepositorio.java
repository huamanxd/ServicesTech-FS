package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.modelo.EstadoOrden;

import java.util.Optional;

public interface EstadoOrdenRepositorio extends JpaRepository<EstadoOrden, Long> {
    Optional<EstadoOrden> findByNombre(EstadoOrden.Codigo nombre);
}
