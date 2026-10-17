package pe.fullstack.servicesbackend.portal.repositorio;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.portal.modelo.Servicio;
import java.util.List;
public interface ServicioRepositorio extends JpaRepository<Servicio,Long> {
 List<Servicio> findByActivoTrueOrderByIdAsc();
 List<Servicio> findAllByOrderByIdAsc();
 boolean existsByCodigoIgnoreCase(String codigo);
}
