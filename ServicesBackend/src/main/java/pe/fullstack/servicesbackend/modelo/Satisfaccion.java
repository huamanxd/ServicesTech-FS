package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Tabla "satisfaccion": calificación del cliente (1 a 5), una por orden. */
@Entity
@Table(name = "satisfaccion")
@Getter
@Setter
@NoArgsConstructor
public class Satisfaccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_satisfaccion")
    private Long id;

    @Column(nullable = false)
    private Short calificacion;

    @Column(length = 200)
    private String comentario;

    @Column(name = "fecha_evaluacion", nullable = false)
    private Instant fechaEvaluacion = Instant.now();

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_orden", unique = true)
    private Orden orden;
}
