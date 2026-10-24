package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.fullstack.servicesbackend.modelo.Rol;

import java.util.Optional;

public interface RolRepositorio extends JpaRepository<Rol, Long> {
    Optional<Rol> findByNombre(Rol.Codigo nombre);
}
