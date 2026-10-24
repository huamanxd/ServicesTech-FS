package pe.fullstack.servicesbackend.servicio;

import org.junit.jupiter.api.Test;
import pe.fullstack.servicesbackend.modelo.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MetricasTest {

    private static final Instant T0 = Instant.parse("2026-10-01T08:00:00Z");

    private static EstadoOrden estado(EstadoOrden.Codigo c) {
        EstadoOrden e = new EstadoOrden();
        e.setNombre(c);
        return e;
    }

    /** Orden creada en T0 con límite a las {@code horasLimite}; resuelta a las {@code horasSolucion} (o null). */
    private static Orden orden(EstadoOrden.Codigo c, int horasLimite, Integer horasSolucion, Integer calificacion) {
        Orden o = new Orden();
        o.setFechaCreacion(T0);
        o.setFechaLimite(T0.plus(Duration.ofHours(horasLimite)));
        o.setEstado(estado(c));
        if (horasSolucion != null) {
            SolucionServicio s = new SolucionServicio();
            s.setFechaSolucion(T0.plus(Duration.ofHours(horasSolucion)));
            o.setSolucion(s);
        }
        if (calificacion != null) {
            Satisfaccion sat = new Satisfaccion();
            sat.setCalificacion(calificacion.shortValue());
            o.setSatisfaccion(sat);
        }
        return o;
    }

    @Test
    void calculaTiemposCumplimientoRetrasosYSatisfaccion() {
        Instant ahora = T0.plus(Duration.ofHours(100));
        List<Orden> ordenes = List.of(
                orden(EstadoOrden.Codigo.CERRADA, 24, 10, 5),     // a tiempo
                orden(EstadoOrden.Codigo.CERRADA, 24, 30, 3),     // resuelta tarde
                orden(EstadoOrden.Codigo.EN_ATENCION, 48, null, null), // abierta y vencida
                orden(EstadoOrden.Codigo.ASIGNADA, 200, null, null),   // abierta a tiempo
                orden(EstadoOrden.Codigo.CANCELADA, 1, null, null));   // no cuenta como retraso

        var i = Metricas.indicadores(ordenes, ahora);

        assertThat(i.ordenes()).isEqualTo(5);
        assertThat(i.abiertas()).isEqualTo(2);
        assertThat(i.resueltas()).isEqualTo(2);
        assertThat(i.retrasadas()).isEqualTo(2);
        assertThat(i.tiempoPromedioHoras()).isEqualTo(20.0);
        assertThat(i.cumplimientoPct()).isEqualTo(50.0);
        assertThat(i.satisfaccionPromedio()).isEqualTo(4.0);
        assertThat(i.evaluaciones()).isEqualTo(2);
    }

    @Test
    void sinDatosLosPromediosSonNulos() {
        var i = Metricas.indicadores(List.of(), T0);
        assertThat(i.tiempoPromedioHoras()).isNull();
        assertThat(i.cumplimientoPct()).isNull();
        assertThat(i.satisfaccionPromedio()).isNull();
    }
}
