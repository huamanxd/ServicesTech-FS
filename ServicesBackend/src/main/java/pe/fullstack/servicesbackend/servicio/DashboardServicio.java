package pe.fullstack.servicesbackend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fullstack.servicesbackend.dto.Respuestas.*;
import pe.fullstack.servicesbackend.dto.VistaMapper;
import pe.fullstack.servicesbackend.modelo.*;
import pe.fullstack.servicesbackend.repositorio.OrdenRepositorio;
import pe.fullstack.servicesbackend.repositorio.SolicitudRepositorio;
import pe.fullstack.servicesbackend.repositorio.UsuarioRepositorio;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Dashboards: administrador (gestión), cliente (solo su información) y técnico (su carga). */
@Service
@RequiredArgsConstructor
public class DashboardServicio {

    private final OrdenRepositorio ordenes;
    private final SolicitudRepositorio solicitudes;
    private final UsuarioRepositorio usuarios;
    private final VistaMapper mapper;
    private final Clock reloj;

    @Transactional(readOnly = true)
    public DashboardAdmin administrador() {
        Instant ahora = reloj.instant();
        List<Orden> todas = ordenes.findAllByOrderByFechaCreacionDesc();
        List<Solicitud> listaSolicitudes = solicitudes.findAllByOrderByFechaSolicitudDesc();
        Set<Long> conOrden = todas.stream().map(o -> o.getSolicitud().getId()).collect(Collectors.toSet());

        Map<String, Long> porEstadoSolicitud = contar(EstadoSolicitud.Codigo.values(),
                listaSolicitudes.stream().map(s -> s.getEstado().getNombre()).toList());
        long porRevisar = porEstadoSolicitud.get("PENDIENTE") + porEstadoSolicitud.get("EN_REVISION");
        long aprobadasSinOrden = listaSolicitudes.stream()
                .filter(s -> s.getEstado().getNombre() == EstadoSolicitud.Codigo.APROBADA && !conOrden.contains(s.getId()))
                .count();

        List<OrdenRetrasada> retrasadas = todas.stream()
                .filter(o -> Metricas.esAbiertaSinResolver(o) && o.retrasada(ahora))
                .sorted(Comparator.comparing(Orden::getFechaLimite))
                .limit(10)
                .map(o -> new OrdenRetrasada(o.getId(), o.getSolicitud().getAsunto(),
                        o.getSolicitud().getPrioridad().getNombre().name(), o.codigoEstado().name(), o.getFechaLimite(),
                        mapper.resumen(o.tecnicoResponsable().orElse(null)),
                        Metricas.redondear(OptionalDouble.of(Metricas.horas(o.getFechaLimite(), ahora)), 1)))
                .toList();

        Map<Integer, Long> calificaciones = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) calificaciones.put(i, 0L);
        todas.stream().map(Orden::getSatisfaccion).filter(Objects::nonNull)
                .forEach(s -> calificaciones.merge((int) s.getCalificacion(), 1L, Long::sum));

        List<Conteo> servicios = listaSolicitudes.stream()
                .collect(Collectors.groupingBy(s -> s.getServicio().getNombre(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.comparingByKey()))
                .limit(8)
                .map(e -> new Conteo(e.getKey(), e.getValue()))
                .toList();

        return new DashboardAdmin(Metricas.indicadores(todas, ahora), listaSolicitudes.size(), porRevisar,
                aprobadasSinOrden, porEstadoSolicitud,
                contar(EstadoOrden.Codigo.values(), todas.stream().map(Orden::codigoEstado).toList()),
                retrasadas, rendimientoTecnicos(todas, ahora), servicios, calificaciones);
    }

    @Transactional(readOnly = true)
    public DashboardCliente cliente(Long clienteId) {
        Instant ahora = reloj.instant();
        List<Solicitud> propias = solicitudes.findByClienteIdOrderByFechaSolicitudDesc(clienteId);
        List<Orden> suyas = ordenes.delCliente(clienteId);
        Map<Long, Orden> porSolicitud = suyas.stream()
                .collect(Collectors.toMap(o -> o.getSolicitud().getId(), Function.identity()));

        // Estado de atención: el de la orden si existe; si no, el de la solicitud.
        Map<String, Long> porEstado = new LinkedHashMap<>();
        for (Solicitud s : propias) {
            Orden o = porSolicitud.get(s.getId());
            String estado = o != null ? o.codigoEstado().name() : s.getEstado().getNombre().name();
            porEstado.merge(estado, 1L, Long::sum);
        }
        long pendientesDeCalificar = suyas.stream()
                .filter(o -> (o.codigoEstado() == EstadoOrden.Codigo.RESUELTA || o.codigoEstado() == EstadoOrden.Codigo.CERRADA)
                        && o.getSatisfaccion() == null)
                .count();
        return new DashboardCliente(Metricas.indicadores(suyas, ahora), propias.size(), porEstado, pendientesDeCalificar);
    }

    @Transactional(readOnly = true)
    public DashboardTecnico tecnico(Long tecnicoId) {
        Instant ahora = reloj.instant();
        List<Orden> propias = ordenesResueltasOVigentesDe(tecnicoId, ordenes.delTecnico(tecnicoId));
        long porIniciar = propias.stream().filter(o -> o.codigoEstado() == EstadoOrden.Codigo.ASIGNADA).count();
        long enAtencion = propias.stream().filter(o -> o.codigoEstado() == EstadoOrden.Codigo.EN_ATENCION).count();
        return new DashboardTecnico(Metricas.indicadores(propias, ahora), porIniciar, enAtencion,
                ordenes.disponibles().size());
    }

    private List<RendimientoTecnico> rendimientoTecnicos(List<Orden> todas, Instant ahora) {
        List<RendimientoTecnico> filas = new ArrayList<>();
        for (Usuario t : usuarios.buscarPorRol(Rol.Codigo.TECNICO)) {
            List<Orden> suyas = ordenesResueltasOVigentesDe(t.getId(), todas);
            List<Orden> resueltas = suyas.stream().filter(o -> o.fechaResolucion().isPresent()).toList();
            filas.add(new RendimientoTecnico(mapper.resumen(t), t.isActivo(), suyas.size(),
                    suyas.stream().filter(Metricas::esAbiertaSinResolver).count(),
                    resueltas.size(),
                    suyas.stream().filter(o -> o.retrasada(ahora)).count(),
                    Metricas.tiempoPromedioHoras(resueltas),
                    Metricas.cumplimientoPct(resueltas),
                    Metricas.redondear(suyas.stream().map(Orden::getSatisfaccion).filter(Objects::nonNull)
                            .mapToInt(Satisfaccion::getCalificacion).average(), 2)));
        }
        filas.sort(Comparator.comparing(RendimientoTecnico::resueltas).reversed());
        return filas;
    }

    /**
     * Órdenes que se atribuyen al técnico (sin canceladas): las que resolvió o las que tiene asignadas ahora.
     * Así una reasignación no cuenta dos veces la misma orden.
     */
    private static List<Orden> ordenesResueltasOVigentesDe(Long tecnicoId, List<Orden> candidatas) {
        return candidatas.stream().filter(o -> o.codigoEstado() != EstadoOrden.Codigo.CANCELADA)
                .filter(o -> o.getSolucion() != null
                        ? o.getSolucion().getTecnico().getId().equals(tecnicoId)
                        : o.asignacionVigente().map(a -> a.getTecnico().getId().equals(tecnicoId)).orElse(false))
                .toList();
    }

    private static <E extends Enum<E>> Map<String, Long> contar(E[] valores, List<E> datos) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (E v : valores) mapa.put(v.name(), 0L);
        datos.forEach(d -> mapa.merge(d.name(), 1L, Long::sum));
        return mapa;
    }
}
