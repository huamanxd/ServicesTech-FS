package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

/** Tabla "solicitudes": lo que registra el cliente. El administrador la revisa y la convierte en orden. */
@Entity
@Table(name = "solicitudes")
@Getter
@Setter
@NoArgsConstructor
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Long id;

    @Column(nullable = false, length = 150)
    private String asunto;

    @Column(nullable = false, length = 700)
    private String descripcion;

    @Column(length = 220)
    private String direccion;

    /** Precio del servicio al momento de solicitarlo (se conserva aunque cambie el catálogo). */
    @Column(name = "precio_referencial", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioReferencial;

    @Column(name = "fecha_solicitud", nullable = false)
    private Instant fechaSolicitud = Instant.now();

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado_solicitud")
    private EstadoSolicitud estado;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_prioridad")
    private Prioridad prioridad;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario")
    private Usuario cliente;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio")
    private Servicio servicio;
}
