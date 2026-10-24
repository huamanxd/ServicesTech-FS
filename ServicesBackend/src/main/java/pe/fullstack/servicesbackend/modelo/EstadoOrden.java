package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Catálogo "estados_orden" del ERD. */
@Entity
@Table(name = "estados_orden")
@Getter
@Setter
@NoArgsConstructor
public class EstadoOrden {

    public enum Codigo {
        PENDIENTE, ASIGNADA, EN_ATENCION, RESUELTA, CERRADA, CANCELADA;

        /** Una orden está abierta mientras no tenga fecha de cierre. */
        public boolean abierta() {
            return this != CERRADA && this != CANCELADA;
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private Codigo nombre;

    @Column(length = 150)
    private String descripcion;
}
