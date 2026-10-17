package pe.fullstack.servicesbackend.portal.repositorio;
import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.portal.modelo.Usuario;
import java.util.Optional;
public interface UsuarioRepositorio extends JpaRepository<Usuario,Long> {
 Optional<Usuario> findByEmailIgnoreCase(String email);
}
