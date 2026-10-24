package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.modelo.Servicio;

import java.util.List;

public interface ServicioRepositorio extends JpaRepository<Servicio, Long> {

    List<Servicio> findByActivoTrueOrderByIdAsc();

    List<Servicio> findAllByOrderByIdAsc();

    boolean existsByCodigoIgnoreCase(String codigo);
}
