package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Catálogo "prioridad" del ERD. horas_objetivo define la fecha límite sugerida de la orden. */
@Entity
@Table(name = "prioridad")
@Getter
@Setter
@NoArgsConstructor
public class Prioridad {

    public enum Codigo { BAJA, MEDIA, ALTA, CRITICA }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_prioridad")
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 30)
    private Codigo nombre;

    @Column(length = 150)
    private String descripcion;

    @Column(name = "horas_objetivo", nullable = false)
    private Integer horasObjetivo;
}
