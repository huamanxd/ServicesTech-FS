package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Tabla "detalle_orden": servicios incluidos en una orden. */
@Entity
@Table(name = "detalle_orden")
@Getter
@Setter
@NoArgsConstructor
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle_orden")
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden")
    private Orden orden;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_servicio")
    private Servicio servicio;

    @Column(name = "precio_referencial", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioReferencial;
}
