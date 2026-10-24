package pe.fullstack.servicesbackend.servicio;

import pe.fullstack.servicesbackend.dto.Respuestas.Indicadores;
import pe.fullstack.servicesbackend.modelo.EstadoOrden;
import pe.fullstack.servicesbackend.modelo.Orden;
import pe.fullstack.servicesbackend.modelo.Satisfaccion;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.OptionalDouble;

/**
 * Cálculo de indicadores sobre un conjunto de órdenes.
 * <ul>
 *   <li>Tiempo de atención: desde la creación de la orden hasta que el técnico registra la solución.</li>
 *   <li>Cumplimiento: % de órdenes resueltas dentro de su fecha límite.</li>
 *   <li>Retrasada: resuelta después del límite, o sin resolver con el límite vencido (no cuenta canceladas).</li>
 * </ul>
 */
public final class Metricas {

    private Metricas() {
    }

    public static Indicadores indicadores(List<Orden> ordenes, Instant ahora) {
        List<Orden> resueltas = ordenes.stream().filter(o -> o.fechaResolucion().isPresent()).toList();
        List<Satisfaccion> evaluaciones = ordenes.stream().map(Orden::getSatisfaccion).filter(Objects::nonNull).toList();
        return new Indicadores(
                ordenes.size(),
                ordenes.stream().filter(o -> o.codigoEstado().abierta()).count(),
                resueltas.size(),
                ordenes.stream().filter(o -> o.retrasada(ahora)).count(),
                tiempoPromedioHoras(resueltas),
                cumplimientoPct(resueltas),
                redondear(evaluaciones.stream().mapToInt(Satisfaccion::getCalificacion).average(), 2),
                evaluaciones.size());
    }

    public static Double tiempoPromedioHoras(List<Orden> resueltas) {
        return redondear(resueltas.stream()
                .mapToDouble(o -> horas(o.getFechaCreacion(), o.fechaResolucion().orElseThrow()))
                .average(), 1);
    }

    public static Double cumplimientoPct(List<Orden> resueltas) {
        if (resueltas.isEmpty()) return null;
        long aTiempo = resueltas.stream().filter(o -> !o.fechaResolucion().orElseThrow().isAfter(o.getFechaLimite())).count();
        return redondear(OptionalDouble.of(aTiempo * 100.0 / resueltas.size()), 1);
    }

    public static double horas(Instant desde, Instant hasta) {
        return Duration.between(desde, hasta).toMinutes() / 60.0;
    }

    public static boolean esAbiertaSinResolver(Orden o) {
        return o.codigoEstado().abierta() && o.codigoEstado() != EstadoOrden.Codigo.RESUELTA;
    }

    public static Double redondear(OptionalDouble valor, int decimales) {
        if (valor.isEmpty()) return null;
        double factor = Math.pow(10, decimales);
        return Math.round(valor.getAsDouble() * factor) / factor;
    }
}
