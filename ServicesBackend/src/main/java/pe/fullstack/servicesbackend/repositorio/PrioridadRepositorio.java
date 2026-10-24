package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.modelo.Prioridad;

import java.util.List;

public interface PrioridadRepositorio extends JpaRepository<Prioridad, Long> {
    List<Prioridad> findAllByOrderByIdAsc();
}
