package pe.fullstack.servicesbackend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas.SolicitudVista;
import pe.fullstack.servicesbackend.dto.VistaMapper;
import pe.fullstack.servicesbackend.modelo.*;
import pe.fullstack.servicesbackend.repositorio.OrdenRepositorio;
import pe.fullstack.servicesbackend.repositorio.SolicitudRepositorio;
import pe.fullstack.servicesbackend.repositorio.UsuarioRepositorio;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Solicitudes: el cliente las registra y cancela; el administrador las revisa. */
@Service
@RequiredArgsConstructor
public class SolicitudServicio {

    private final SolicitudRepositorio solicitudes;
    private final OrdenRepositorio ordenes;
    private final UsuarioRepositorio usuarios;
    private final CatalogoServicio catalogo;
    private final VistaMapper mapper;
    private final Clock reloj;

    @Transactional
    public SolicitudVista registrar(Long clienteId, Peticiones.NuevaSolicitud p) {
        Servicio servicio = catalogo.servicio(p.servicioId());
        if (!servicio.isActivo()) throw NegocioExcepcion.conflicto("El servicio ya no está disponible");
        Solicitud s = new Solicitud();
        s.setCliente(usuarios.getReferenceById(clienteId));
        s.setServicio(servicio);
        s.setPrioridad(catalogo.prioridad(p.prioridadId()));
        s.setAsunto(p.asunto().strip());
        s.setDescripcion(p.descripcion().strip());
        s.setDireccion(p.direccion() == null || p.direccion().isBlank() ? null : p.direccion().strip());
        s.setPrecioReferencial(servicio.getPrecioReferencial());
        s.setFechaSolicitud(reloj.instant());
        s.setEstado(catalogo.estado(EstadoSolicitud.Codigo.PENDIENTE));
        solicitudes.saveAndFlush(s);
        return mapper.solicitud(s, null, reloj.instant());
    }

    /** Historial del cliente: solo sus propias solicitudes, con la orden asociada si existe. */
    @Transactional(readOnly = true)
    public List<SolicitudVista> delCliente(Long clienteId) {
        return vistas(solicitudes.findByClienteIdOrderByFechaSolicitudDesc(clienteId));
    }

    @Transactional(readOnly = true)
    public List<SolicitudVista> todas(EstadoSolicitud.Codigo estado) {
        return vistas(estado == null ? solicitudes.findAllByOrderByFechaSolicitudDesc()
                : solicitudes.findByEstadoNombreOrderByFechaSolicitudDesc(estado));
    }

    @Transactional
    public SolicitudVista cancelarPorCliente(Long clienteId, Long solicitudId) {
        Solicitud s = solicitud(solicitudId);
        if (!s.getCliente().getId().equals(clienteId)) throw NegocioExcepcion.noEncontrado("Solicitud no encontrada");
        if (ordenes.existsBySolicitudId(s.getId())) {
            throw NegocioExcepcion.conflicto("La solicitud ya tiene una orden de servicio; comunícate con la empresa para cancelarla");
        }
        cambiarEstado(s, EstadoSolicitud.Codigo.CANCELADA);
        return mapper.solicitud(s, null, reloj.instant());
    }

    @Transactional
    public SolicitudVista revisar(Long solicitudId, Peticiones.Revision p) {
        Solicitud s = solicitud(solicitudId);
        cambiarEstado(s, EstadoSolicitud.Codigo.valueOf(p.estado()));
        return mapper.solicitud(s, ordenes.findBySolicitudId(s.getId()).orElse(null), reloj.instant());
    }

    Solicitud solicitud(Long id) {
        return solicitudes.findById(id).orElseThrow(() -> NegocioExcepcion.noEncontrado("Solicitud no encontrada"));
    }

    private void cambiarEstado(Solicitud s, EstadoSolicitud.Codigo nuevo) {
        ReglasEstado.validar(s.getEstado().getNombre(), nuevo);
        s.setEstado(catalogo.estado(nuevo));
    }

    private List<SolicitudVista> vistas(List<Solicitud> lista) {
        Instant ahora = reloj.instant();
        Map<Long, Orden> porSolicitud = lista.isEmpty() ? Map.of()
                : ordenes.findBySolicitudIdIn(lista.stream().map(Solicitud::getId).toList()).stream()
                .collect(Collectors.toMap(o -> o.getSolicitud().getId(), Function.identity()));
        return lista.stream().map(s -> mapper.solicitud(s, porSolicitud.get(s.getId()), ahora)).toList();
    }
}
