package pe.fullstack.servicesbackend.servicio;

import org.junit.jupiter.api.Test;
import pe.fullstack.servicesbackend.modelo.EstadoOrden;
import pe.fullstack.servicesbackend.modelo.EstadoSolicitud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static pe.fullstack.servicesbackend.modelo.EstadoOrden.Codigo.*;

class ReglasEstadoTest {

    @Test
    void flujoNormalDeLaOrdenEstaPermitido() {
        assertThat(ReglasEstado.permitida(PENDIENTE, ASIGNADA)).isTrue();
        assertThat(ReglasEstado.permitida(ASIGNADA, EN_ATENCION)).isTrue();
        assertThat(ReglasEstado.permitida(EN_ATENCION, RESUELTA)).isTrue();
        assertThat(ReglasEstado.permitida(RESUELTA, CERRADA)).isTrue();
    }

    @Test
    void reasignacionYCancelacionSoloAntesDeResolver() {
        assertThat(ReglasEstado.permitida(ASIGNADA, ASIGNADA)).isTrue();
        assertThat(ReglasEstado.permitida(EN_ATENCION, ASIGNADA)).isTrue();
        assertThat(ReglasEstado.permitida(EN_ATENCION, CANCELADA)).isTrue();
        assertThat(ReglasEstado.permitida(RESUELTA, CANCELADA)).isFalse();
        assertThat(ReglasEstado.permitida(RESUELTA, ASIGNADA)).isFalse();
    }

    @Test
    void estadosFinalesNoCambian() {
        for (EstadoOrden.Codigo destino : EstadoOrden.Codigo.values()) {
            assertThat(ReglasEstado.permitida(CERRADA, destino)).isFalse();
            assertThat(ReglasEstado.permitida(CANCELADA, destino)).isFalse();
        }
    }

    @Test
    void saltarEstadosDaConflicto() {
        assertThatThrownBy(() -> ReglasEstado.validar(PENDIENTE, RESUELTA))
                .isInstanceOf(NegocioExcepcion.class)
                .hasMessageContaining("PENDIENTE");
    }

    @Test
    void solicitudRechazadaNoPuedeAprobarse() {
        assertThat(ReglasEstado.permitida(EstadoSolicitud.Codigo.PENDIENTE, EstadoSolicitud.Codigo.APROBADA)).isTrue();
        assertThat(ReglasEstado.permitida(EstadoSolicitud.Codigo.RECHAZADA, EstadoSolicitud.Codigo.APROBADA)).isFalse();
        assertThat(ReglasEstado.permitida(EstadoSolicitud.Codigo.APROBADA, EstadoSolicitud.Codigo.RECHAZADA)).isFalse();
    }
}
