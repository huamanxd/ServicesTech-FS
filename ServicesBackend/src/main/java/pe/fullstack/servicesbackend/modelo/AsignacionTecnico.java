package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Tabla "asignaciones_tecnico". Vigente mientras fecha_fin sea NULL. */
@Entity
@Table(name = "asignaciones_tecnico")
@Getter
@Setter
@NoArgsConstructor
public class AsignacionTecnico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_asignacion")
    private Long id;

    @Column(name = "fecha_asignacion", nullable = false)
    private Instant fechaAsignacion = Instant.now();

    @Column(name = "fecha_fin")
    private Instant fechaFin;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario tecnico;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden")
    private Orden orden;

    public boolean vigente() {
        return fechaFin == null;
    }
}
