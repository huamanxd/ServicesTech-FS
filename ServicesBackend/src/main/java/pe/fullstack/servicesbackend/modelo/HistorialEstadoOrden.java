package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Tabla "historial_estado_orden": bitácora de cada cambio de estado. */
@Entity
@Table(name = "historial_estado_orden")
@Getter
@Setter
@NoArgsConstructor
public class HistorialEstadoOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_historial")
    private Long id;

    @Column(name = "fecha_cambio", nullable = false)
    private Instant fechaCambio = Instant.now();

    @Column(length = 200)
    private String observacion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario usuario;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden")
    private Orden orden;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado")
    private EstadoOrden estado;
}
