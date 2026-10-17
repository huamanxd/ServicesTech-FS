package pe.fullstack.servicesbackend.portal.repositorio;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.portal.modelo.Solicitud;
import java.util.List;
public interface SolicitudRepositorio extends JpaRepository<Solicitud,Long> {
 List<Solicitud> findByClienteIdOrderByCreadoEnDesc(Long clienteId);
 List<Solicitud> findAllByOrderByCreadoEnDesc();
}
