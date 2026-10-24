package pe.fullstack.servicesbackend.servicio;

import pe.fullstack.servicesbackend.modelo.EstadoOrden;
import pe.fullstack.servicesbackend.modelo.EstadoSolicitud;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static pe.fullstack.servicesbackend.modelo.EstadoOrden.Codigo.*;

/**
 * Transiciones permitidas. Cualquier otra se rechaza con 409.
 *
 * <pre>
 * Solicitud: PENDIENTE -> EN_REVISION | APROBADA | RECHAZADA | CANCELADA
 *            EN_REVISION -> APROBADA | RECHAZADA | CANCELADA
 *            APROBADA -> CANCELADA (solo si todavía no tiene orden)
 *
 * Orden:     PENDIENTE -> ASIGNADA | CANCELADA
 *            ASIGNADA -> ASIGNADA (reasignación) | EN_ATENCION | CANCELADA
 *            EN_ATENCION -> ASIGNADA (reasignación) | RESUELTA | CANCELADA
 *            RESUELTA -> CERRADA
 * </pre>
 */
public final class ReglasEstado {

    private ReglasEstado() {
    }

    private static final Map<EstadoSolicitud.Codigo, Set<EstadoSolicitud.Codigo>> SOLICITUD =
            new EnumMap<>(EstadoSolicitud.Codigo.class);
    private static final Map<EstadoOrden.Codigo, Set<EstadoOrden.Codigo>> ORDEN = new EnumMap<>(EstadoOrden.Codigo.class);

    static {
        SOLICITUD.put(EstadoSolicitud.Codigo.PENDIENTE, EnumSet.of(EstadoSolicitud.Codigo.EN_REVISION,
                EstadoSolicitud.Codigo.APROBADA, EstadoSolicitud.Codigo.RECHAZADA, EstadoSolicitud.Codigo.CANCELADA));
        SOLICITUD.put(EstadoSolicitud.Codigo.EN_REVISION, EnumSet.of(EstadoSolicitud.Codigo.APROBADA,
                EstadoSolicitud.Codigo.RECHAZADA, EstadoSolicitud.Codigo.CANCELADA));
        SOLICITUD.put(EstadoSolicitud.Codigo.APROBADA, EnumSet.of(EstadoSolicitud.Codigo.CANCELADA));
        SOLICITUD.put(EstadoSolicitud.Codigo.RECHAZADA, EnumSet.noneOf(EstadoSolicitud.Codigo.class));
        SOLICITUD.put(EstadoSolicitud.Codigo.CANCELADA, EnumSet.noneOf(EstadoSolicitud.Codigo.class));

        ORDEN.put(PENDIENTE, EnumSet.of(ASIGNADA, CANCELADA));
        ORDEN.put(ASIGNADA, EnumSet.of(ASIGNADA, EN_ATENCION, CANCELADA));
        ORDEN.put(EN_ATENCION, EnumSet.of(ASIGNADA, RESUELTA, CANCELADA));
        ORDEN.put(RESUELTA, EnumSet.of(CERRADA));
        ORDEN.put(CERRADA, EnumSet.noneOf(EstadoOrden.Codigo.class));
        ORDEN.put(CANCELADA, EnumSet.noneOf(EstadoOrden.Codigo.class));
    }

    public static boolean permitida(EstadoSolicitud.Codigo actual, EstadoSolicitud.Codigo nuevo) {
        return SOLICITUD.get(actual).contains(nuevo);
    }

    public static boolean permitida(EstadoOrden.Codigo actual, EstadoOrden.Codigo nuevo) {
        return ORDEN.get(actual).contains(nuevo);
    }

    public static void validar(EstadoSolicitud.Codigo actual, EstadoSolicitud.Codigo nuevo) {
        if (!permitida(actual, nuevo)) {
            throw NegocioExcepcion.conflicto("La solicitud está " + actual + " y no puede pasar a " + nuevo);
        }
    }

    public static void validar(EstadoOrden.Codigo actual, EstadoOrden.Codigo nuevo) {
        if (!permitida(actual, nuevo)) {
            throw NegocioExcepcion.conflicto("La orden está " + actual + " y no puede pasar a " + nuevo);
        }
    }
}
