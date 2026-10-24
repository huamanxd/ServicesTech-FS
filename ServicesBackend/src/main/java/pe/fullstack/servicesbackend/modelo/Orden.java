package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Tabla "ordenes": se genera a partir de una solicitud aprobada.
 * Agrupa su detalle, las asignaciones de técnicos, el historial, la solución y la satisfacción.
 */
@Entity
@Table(name = "ordenes")
@Getter
@Setter
@NoArgsConstructor
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_orden")
    private Long id;

    @Column(name = "fecha_creacion", nullable = false)
    private Instant fechaCreacion = Instant.now();

    @Column(name = "fecha_limite", nullable = false)
    private Instant fechaLimite;

    @Column(name = "fecha_cierre")
    private Instant fechaCierre;

    @Column(length = 200)
    private String observaciones;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_solicitud", unique = true)
    private Solicitud solicitud;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "id_estado")
    private EstadoOrden estado;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    @OrderBy("id")
    private List<DetalleOrden> detalles = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    @OrderBy("fechaAsignacion")
    private List<AsignacionTecnico> asignaciones = new ArrayList<>();

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL)
    @OrderBy("fechaCambio, id")
    private List<HistorialEstadoOrden> historial = new ArrayList<>();

    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private SolucionServicio solucion;

    @OneToOne(mappedBy = "orden", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Satisfaccion satisfaccion;

    public EstadoOrden.Codigo codigoEstado() {
        return estado.getNombre();
    }

    public Optional<AsignacionTecnico> asignacionVigente() {
        return asignaciones.stream().filter(AsignacionTecnico::vigente).findFirst();
    }

    /** Técnico vigente o, si ya terminó, el último que la atendió. */
    public Optional<Usuario> tecnicoResponsable() {
        return asignacionVigente()
                .or(() -> asignaciones.stream().max(Comparator.comparing(AsignacionTecnico::getFechaAsignacion)))
                .map(AsignacionTecnico::getTecnico);
    }

    public void agregarDetalle(Servicio servicio, BigDecimal precio) {
        DetalleOrden d = new DetalleOrden();
        d.setOrden(this);
        d.setServicio(servicio);
        d.setPrecioReferencial(precio);
        detalles.add(d);
    }

    public BigDecimal total() {
        return detalles.stream().map(DetalleOrden::getPrecioReferencial).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Cambia el estado y deja constancia en el historial. */
    public void registrarEstado(EstadoOrden nuevo, Usuario usuario, String observacion, Instant cuando) {
        this.estado = nuevo;
        HistorialEstadoOrden h = new HistorialEstadoOrden();
        h.setOrden(this);
        h.setEstado(nuevo);
        h.setUsuario(usuario);
        h.setObservacion(observacion);
        h.setFechaCambio(cuando);
        historial.add(h);
        if (!nuevo.getNombre().abierta() && fechaCierre == null) {
            fechaCierre = cuando;
        }
    }

    /** Momento en que el técnico resolvió la orden (registro de la solución), si ya ocurrió. */
    public Optional<Instant> fechaResolucion() {
        return Optional.ofNullable(solucion).map(SolucionServicio::getFechaSolucion);
    }

    /**
     * Retraso = la orden se resolvió después de la fecha límite, o sigue sin resolverse
     * y la fecha límite ya pasó. Las canceladas no cuentan.
     */
    public boolean retrasada(Instant ahora) {
        if (codigoEstado() == EstadoOrden.Codigo.CANCELADA) return false;
        return fechaResolucion().orElse(ahora).isAfter(fechaLimite);
    }
}
