package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Catálogo "servicios" (ERD + código, categoría, precio y duración referenciales). */
@Entity
@Table(name = "servicios")
@Getter
@Setter
@NoArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Long id;

    @Column(nullable = false, unique = true, length = 45)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false, length = 600)
    private String descripcion;

    @Column(nullable = false, length = 25)
    private String categoria;

    @Column(name = "precio_referencial", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioReferencial;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos;

    @Column(nullable = false)
    private boolean activo = true;
}
