package pe.fullstack.servicesbackend.repositorio;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import pe.fullstack.servicesbackend.modelo.EstadoOrden;
import pe.fullstack.servicesbackend.modelo.Orden;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrdenRepositorio extends JpaRepository<Orden, Long> {

    @EntityGraph(attributePaths = {"estado", "solicitud", "solicitud.cliente", "solicitud.prioridad"})
    List<Orden> findAllByOrderByFechaCreacionDesc();

    @EntityGraph(attributePaths = {"estado", "solicitud", "solicitud.cliente", "solicitud.prioridad"})
    List<Orden> findByEstadoNombreOrderByFechaCreacionDesc(EstadoOrden.Codigo estado);

    @EntityGraph(attributePaths = {"estado", "solicitud", "solicitud.cliente", "solicitud.prioridad"})
    List<Orden> findBySolicitudIdIn(Collection<Long> solicitudIds);

    Optional<Orden> findBySolicitudId(Long solicitudId);

    boolean existsBySolicitudId(Long solicitudId);

    @EntityGraph(attributePaths = {"estado", "solicitud", "solicitud.cliente", "solicitud.prioridad"})
    @Query("select o from Orden o where o.solicitud.cliente.id = :clienteId order by o.fechaCreacion desc")
    List<Orden> delCliente(Long clienteId);

    /** Órdenes en las que el técnico tiene o tuvo una asignación. */
    @EntityGraph(attributePaths = {"estado", "solicitud", "solicitud.cliente", "solicitud.prioridad"})
    @Query("select distinct o from Orden o join o.asignaciones a where a.tecnico.id = :tecnicoId order by o.fechaCreacion desc")
    List<Orden> delTecnico(Long tecnicoId);

    /** Bandeja: órdenes pendientes y sin técnico vigente. */
    @EntityGraph(attributePaths = {"estado", "solicitud", "solicitud.cliente", "solicitud.prioridad"})
    @Query("""
            select o from Orden o
            where o.estado.nombre = pe.fullstack.servicesbackend.modelo.EstadoOrden.Codigo.PENDIENTE
              and not exists (select 1 from AsignacionTecnico a where a.orden = o and a.fechaFin is null)
            order by o.solicitud.prioridad.id desc, o.fechaLimite asc""")
    List<Orden> disponibles();

    @Query("select count(a) from AsignacionTecnico a where a.tecnico.id = :tecnicoId and a.fechaFin is null")
    long asignacionesVigentes(Long tecnicoId);
}
