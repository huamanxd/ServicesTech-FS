package pe.fullstack.servicesbackend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas.HistorialVista;
import pe.fullstack.servicesbackend.dto.Respuestas.OrdenVista;
import pe.fullstack.servicesbackend.dto.VistaMapper;
import pe.fullstack.servicesbackend.modelo.*;
import pe.fullstack.servicesbackend.repositorio.OrdenRepositorio;
import pe.fullstack.servicesbackend.repositorio.UsuarioRepositorio;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static pe.fullstack.servicesbackend.modelo.EstadoOrden.Codigo.*;

/**
 * Ciclo de vida de la orden de servicio:
 * generar (admin) -> asignar/tomar -> iniciar atención (técnico) -> registrar solución (técnico)
 * -> calificar (cliente, cierra la orden) o cerrar (admin). El admin puede cancelar mientras no esté resuelta.
 */
@Service
@RequiredArgsConstructor
public class OrdenServicio {

    private final OrdenRepositorio ordenes;
    private final UsuarioRepositorio usuarios;
    private final SolicitudServicio solicitudes;
    private final UsuarioServicio usuarioServicio;
    private final CatalogoServicio catalogo;
    private final VistaMapper mapper;
    private final Clock reloj;

    // ------------------------------------------------------------------ administrador

    @Transactional
    public OrdenVista generar(Long solicitudId, Peticiones.NuevaOrden p, Long adminId) {
        Solicitud s = solicitudes.solicitud(solicitudId);
        if (s.getEstado().getNombre() != EstadoSolicitud.Codigo.APROBADA) {
            throw NegocioExcepcion.conflicto("Solo se generan órdenes de solicitudes APROBADAS");
        }
        if (ordenes.existsBySolicitudId(s.getId())) {
            throw NegocioExcepcion.conflicto("La solicitud ya tiene una orden");
        }
        Instant ahora = reloj.instant();
        Orden o = new Orden();
        o.setSolicitud(s);
        o.setFechaCreacion(ahora);
        o.setFechaLimite(p.fechaLimite() != null ? p.fechaLimite()
                : ahora.plus(Duration.ofHours(s.getPrioridad().getHorasObjetivo())));
        o.setObservaciones(textoOpcional(p.observaciones()));

        Set<Long> ids = new LinkedHashSet<>();
        if (p.servicioIds() == null || p.servicioIds().isEmpty()) ids.add(s.getServicio().getId());
        else ids.addAll(p.servicioIds());
        for (Long id : ids) {
            Servicio servicio = catalogo.servicio(id);
            boolean esElSolicitado = servicio.getId().equals(s.getServicio().getId());
            if (!servicio.isActivo() && !esElSolicitado) {
                throw NegocioExcepcion.conflicto("El servicio " + servicio.getNombre() + " no está activo");
            }
            // El servicio solicitado conserva el precio que vio el cliente.
            BigDecimal precio = esElSolicitado ? s.getPrecioReferencial() : servicio.getPrecioReferencial();
            o.agregarDetalle(servicio, precio);
        }

        Usuario admin = usuarios.getReferenceById(adminId);
        o.registrarEstado(catalogo.estado(PENDIENTE), admin, "Orden generada", ahora);
        if (p.tecnicoId() != null) {
            asignarTecnico(o, usuarioServicio.tecnicoActivo(p.tecnicoId()), admin, null, ahora);
        }
        ordenes.saveAndFlush(o);
        return mapper.orden(o, ahora);
    }

    @Transactional
    public OrdenVista asignar(Long ordenId, Peticiones.Asignacion p, Long adminId) {
        Orden o = orden(ordenId);
        Instant ahora = reloj.instant();
        asignarTecnico(o, usuarioServicio.tecnicoActivo(p.tecnicoId()), usuarios.getReferenceById(adminId),
                textoOpcional(p.observacion()), ahora);
        return mapper.orden(o, ahora);
    }

    @Transactional
    public OrdenVista cerrar(Long ordenId, String observacion, Long adminId) {
        Orden o = orden(ordenId);
        Instant ahora = reloj.instant();
        cambiar(o, CERRADA, usuarios.getReferenceById(adminId), textoOpcional(observacion, "Orden cerrada"), ahora);
        return mapper.orden(o, ahora);
    }

    @Transactional
    public OrdenVista cancelar(Long ordenId, String motivo, Long adminId) {
        Orden o = orden(ordenId);
        Instant ahora = reloj.instant();
        cambiar(o, CANCELADA, usuarios.getReferenceById(adminId), motivo.strip(), ahora);
        o.asignacionVigente().ifPresent(a -> a.setFechaFin(ahora));
        return mapper.orden(o, ahora);
    }

    @Transactional(readOnly = true)
    public List<OrdenVista> todas(EstadoOrden.Codigo estado) {
        Instant ahora = reloj.instant();
        List<Orden> lista = estado == null ? ordenes.findAllByOrderByFechaCreacionDesc()
                : ordenes.findByEstadoNombreOrderByFechaCreacionDesc(estado);
        return lista.stream().map(o -> mapper.orden(o, ahora)).toList();
    }

    @Transactional(readOnly = true)
    public List<HistorialVista> historial(Long ordenId) {
        return mapper.historial(orden(ordenId).getHistorial());
    }

    // ------------------------------------------------------------------ técnico

    @Transactional(readOnly = true)
    public List<OrdenVista> disponibles() {
        Instant ahora = reloj.instant();
        return ordenes.disponibles().stream().map(o -> mapper.orden(o, ahora)).toList();
    }

    @Transactional(readOnly = true)
    public List<OrdenVista> delTecnico(Long tecnicoId) {
        Instant ahora = reloj.instant();
        return ordenes.delTecnico(tecnicoId).stream().map(o -> mapper.orden(o, ahora)).toList();
    }

    /** El técnico toma una orden de la bandeja (pendiente y sin técnico). */
    @Transactional
    public OrdenVista tomar(Long ordenId, Long tecnicoId) {
        Orden o = orden(ordenId);
        if (o.codigoEstado() != PENDIENTE || o.asignacionVigente().isPresent()) {
            throw NegocioExcepcion.conflicto("La orden ya fue tomada por otro técnico");
        }
        Usuario tecnico = usuarioServicio.tecnicoActivo(tecnicoId);
        Instant ahora = reloj.instant();
        asignarTecnico(o, tecnico, tecnico, "Tomada desde la bandeja de órdenes", ahora);
        return mapper.orden(o, ahora);
    }

    @Transactional
    public OrdenVista iniciarAtencion(Long ordenId, Long tecnicoId, String observacion) {
        Orden o = ordenDelTecnico(ordenId, tecnicoId);
        Instant ahora = reloj.instant();
        cambiar(o, EN_ATENCION, usuarios.getReferenceById(tecnicoId),
                textoOpcional(observacion, "Técnico inició la atención"), ahora);
        return mapper.orden(o, ahora);
    }

    @Transactional
    public OrdenVista registrarSolucion(Long ordenId, Long tecnicoId, String descripcion) {
        Orden o = ordenDelTecnico(ordenId, tecnicoId);
        ReglasEstado.validar(o.codigoEstado(), RESUELTA);
        Instant ahora = reloj.instant();
        Usuario tecnico = usuarios.getReferenceById(tecnicoId);
        SolucionServicio sol = new SolucionServicio();
        sol.setOrden(o);
        sol.setTecnico(tecnico);
        sol.setDescripcionSolucion(descripcion.strip());
        sol.setFechaSolucion(ahora);
        o.setSolucion(sol);
        cambiar(o, RESUELTA, tecnico, "Problema solucionado", ahora);
        o.asignacionVigente().ifPresent(a -> a.setFechaFin(ahora));
        return mapper.orden(o, ahora);
    }

    // ------------------------------------------------------------------ cliente

    /** El cliente califica una orden resuelta; si aún no estaba cerrada, se cierra. */
    @Transactional
    public OrdenVista calificar(Long ordenId, Long clienteId, Peticiones.Calificacion p) {
        Orden o = ordenDelCliente(ordenId, clienteId);
        if (o.codigoEstado() != RESUELTA && o.codigoEstado() != CERRADA) {
            throw NegocioExcepcion.conflicto("Solo puedes calificar un servicio ya resuelto");
        }
        if (o.getSatisfaccion() != null) throw NegocioExcepcion.conflicto("Este servicio ya fue calificado");
        Instant ahora = reloj.instant();
        Satisfaccion sat = new Satisfaccion();
        sat.setOrden(o);
        sat.setCalificacion(p.calificacion().shortValue());
        sat.setComentario(textoOpcional(p.comentario()));
        sat.setFechaEvaluacion(ahora);
        o.setSatisfaccion(sat);
        if (o.codigoEstado() == RESUELTA) {
            cambiar(o, CERRADA, usuarios.getReferenceById(clienteId), "Cerrada tras la evaluación del cliente", ahora);
        }
        return mapper.orden(o, ahora);
    }

    @Transactional(readOnly = true)
    public List<HistorialVista> historialDelCliente(Long ordenId, Long clienteId) {
        return mapper.historial(ordenDelCliente(ordenId, clienteId).getHistorial());
    }

    @Transactional(readOnly = true)
    public List<HistorialVista> historialDelTecnico(Long ordenId, Long tecnicoId) {
        Orden o = orden(ordenId);
        boolean participa = o.getAsignaciones().stream().anyMatch(a -> a.getTecnico().getId().equals(tecnicoId));
        if (!participa && o.codigoEstado() != PENDIENTE) throw NegocioExcepcion.noEncontrado("Orden no encontrada");
        return mapper.historial(o.getHistorial());
    }

    // ------------------------------------------------------------------ reglas internas

    private void asignarTecnico(Orden o, Usuario tecnico, Usuario actor, String observacion, Instant ahora) {
        ReglasEstado.validar(o.codigoEstado(), ASIGNADA);
        o.asignacionVigente().ifPresent(vigente -> {
            if (vigente.getTecnico().getId().equals(tecnico.getId())) {
                throw NegocioExcepcion.conflicto("La orden ya está asignada a " + tecnico.nombreCompleto());
            }
            vigente.setFechaFin(ahora);
        });
        AsignacionTecnico a = new AsignacionTecnico();
        a.setOrden(o);
        a.setTecnico(tecnico);
        a.setFechaAsignacion(ahora);
        o.getAsignaciones().add(a);
        o.registrarEstado(catalogo.estado(ASIGNADA), actor,
                textoOpcional(observacion, "Asignada a " + tecnico.nombreCompleto()), ahora);
    }

    private void cambiar(Orden o, EstadoOrden.Codigo nuevo, Usuario actor, String observacion, Instant ahora) {
        ReglasEstado.validar(o.codigoEstado(), nuevo);
        o.registrarEstado(catalogo.estado(nuevo), actor, observacion, ahora);
    }

    private Orden orden(Long id) {
        return ordenes.findById(id).orElseThrow(() -> NegocioExcepcion.noEncontrado("Orden no encontrada"));
    }

    private Orden ordenDelTecnico(Long ordenId, Long tecnicoId) {
        Orden o = orden(ordenId);
        boolean esSuya = o.asignacionVigente().map(a -> a.getTecnico().getId().equals(tecnicoId)).orElse(false);
        if (!esSuya) throw NegocioExcepcion.prohibido("La orden no está asignada a ti");
        return o;
    }

    private Orden ordenDelCliente(Long ordenId, Long clienteId) {
        Orden o = orden(ordenId);
        if (!o.getSolicitud().getCliente().getId().equals(clienteId)) {
            throw NegocioExcepcion.noEncontrado("Orden no encontrada");
        }
        return o;
    }

    private static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }

    private static String textoOpcional(String texto, String porDefecto) {
        String t = textoOpcional(texto);
        return t == null ? porDefecto : t;
    }
}
