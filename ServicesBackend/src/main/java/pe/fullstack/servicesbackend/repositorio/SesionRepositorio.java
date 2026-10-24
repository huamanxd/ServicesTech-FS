package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import pe.fullstack.servicesbackend.modelo.Sesion;

import java.time.Instant;
import java.util.Optional;

public interface SesionRepositorio extends JpaRepository<Sesion, Long> {

    Optional<Sesion> findByTokenHashAndExpiraEnAfter(String tokenHash, Instant ahora);

    @Modifying
    @Query("delete from Sesion s where s.tokenHash = :tokenHash")
    int eliminarPorHash(String tokenHash);

    @Modifying
    @Query("delete from Sesion s where s.usuario.id = :usuarioId")
    int eliminarDelUsuario(Long usuarioId);

    @Modifying
    @Query("delete from Sesion s where s.expiraEn < :ahora")
    int eliminarVencidas(Instant ahora);
}
