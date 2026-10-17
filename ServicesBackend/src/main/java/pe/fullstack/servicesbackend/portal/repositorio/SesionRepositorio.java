package pe.fullstack.servicesbackend.portal.repositorio;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.portal.modelo.Sesion;
import java.time.Instant;
import java.util.Optional;
public interface SesionRepositorio extends JpaRepository<Sesion,Long> {
 Optional<Sesion> findByTokenHashAndExpiraEnAfter(String hash,Instant now);
 void deleteByTokenHash(String hash);
}
