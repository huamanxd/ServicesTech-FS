package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Catálogo "estado_solicitud" del ERD. */
@Entity
@Table(name = "estado_solicitud")
@Getter
@Setter
@NoArgsConstructor
public class EstadoSolicitud {

    public enum Codigo { PENDIENTE, EN_REVISION, APROBADA, RECHAZADA, CANCELADA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_solicitud")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private Codigo nombre;

    @Column(length = 150)
    private String descripcion;
}
