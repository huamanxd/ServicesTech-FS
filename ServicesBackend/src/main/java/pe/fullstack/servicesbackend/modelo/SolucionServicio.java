package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Tabla "solucion_servicio": lo que hizo el técnico para resolver la orden. */
@Entity
@Table(name = "solucion_servicio")
@Getter
@Setter
@NoArgsConstructor
public class SolucionServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solucion")
    private Long id;

    @Column(name = "descripcion_solucion", nullable = false, length = 500)
    private String descripcionSolucion;

    @Column(name = "fecha_solucion", nullable = false)
    private Instant fechaSolucion = Instant.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario tecnico;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", unique = true)
    private Orden orden;
}
