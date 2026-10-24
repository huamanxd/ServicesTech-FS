package pe.fullstack.servicesbackend.dto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import pe.fullstack.servicesbackend.dto.Respuestas.*;
import pe.fullstack.servicesbackend.modelo.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Conversión entidad -> vista con MapStruct. Las vistas compuestas (orden, solicitud)
 * se arman con métodos por defecto para dejar explícitas las reglas de negocio que muestran.
 */
@Mapper
public interface VistaMapper {

    @Mapping(target = "roles", source = "roles")
    Perfil perfil(Usuario usuario);

    ServicioVista servicio(Servicio servicio);

    @Mapping(target = "nombre", expression = "java(prioridad.getNombre().name())")
    PrioridadVista prioridad(Prioridad prioridad);

    @Mapping(target = "servicioId", source = "servicio.id")
    @Mapping(target = "nombre", source = "servicio.nombre")
    DetalleVista detalle(DetalleOrden detalle);

    @Mapping(target = "estado", expression = "java(h.getEstado().getNombre().name())")
    @Mapping(target = "fecha", source = "fechaCambio")
    HistorialVista historial(HistorialEstadoOrden h);

    @Mapping(target = "descripcion", source = "descripcionSolucion")
    @Mapping(target = "fecha", source = "fechaSolucion")
    SolucionVista solucion(SolucionServicio solucion);

    @Mapping(target = "fecha", source = "fechaEvaluacion")
    SatisfaccionVista satisfaccion(Satisfaccion satisfaccion);

    List<ServicioVista> servicios(List<Servicio> servicios);

    List<PrioridadVista> prioridades(List<Prioridad> prioridades);

    List<HistorialVista> historial(List<HistorialEstadoOrden> historial);

    List<Perfil> perfiles(List<Usuario> usuarios);

    default UsuarioResumen resumen(Usuario usuario) {
        return usuario == null ? null : new UsuarioResumen(usuario.getId(), usuario.nombreCompleto());
    }

    default List<String> roles(Set<Rol> roles) {
        return roles.stream().map(r -> r.getNombre()).sorted(Comparator.comparing(Enum::ordinal)).map(Enum::name).toList();
    }

    default OrdenVista orden(Orden o, Instant ahora) {
        if (o == null) return null;
        Solicitud s = o.getSolicitud();
        return new OrdenVista(o.getId(), s.getId(), s.getAsunto(), s.getDescripcion(), s.getDireccion(),
                s.getPrioridad().getNombre().name(), resumen(s.getCliente()), o.codigoEstado().name(),
                o.getFechaCreacion(), o.getFechaLimite(), o.getFechaCierre(), o.getObservaciones(),
                o.retrasada(ahora), resumen(o.tecnicoResponsable().orElse(null)),
                o.getDetalles().stream().map(this::detalle).toList(), o.total(),
                o.getSolucion() == null ? null : solucion(o.getSolucion()),
                o.getSatisfaccion() == null ? null : satisfaccion(o.getSatisfaccion()));
    }

    default SolicitudVista solicitud(Solicitud s, Orden orden, Instant ahora) {
        return new SolicitudVista(s.getId(), s.getAsunto(), s.getDescripcion(), s.getDireccion(),
                s.getEstado().getNombre().name(), s.getPrioridad().getNombre().name(), s.getServicio().getId(),
                s.getServicio().getNombre(), s.getPrecioReferencial(), s.getFechaSolicitud(),
                resumen(s.getCliente()), orden(orden, ahora));
    }
}
