package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.fullstack.servicesbackend.modelo.Rol;
import pe.fullstack.servicesbackend.modelo.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepositorio extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByDni(String dni);

    List<Usuario> findAllByOrderByIdAsc();

    @Query("select distinct u from Usuario u join u.roles r where r.nombre = :rol order by u.id")
    List<Usuario> buscarPorRol(Rol.Codigo rol);

    @Query("select count(distinct u) from Usuario u join u.roles r where r.nombre = :rol and u.activo = true")
    long contarActivosPorRol(Rol.Codigo rol);
}
