package pe.fullstack.servicesbackend.portal.modelo;
import jakarta.persistence.*;
import java.time.Instant;
import java.math.BigDecimal;
@Entity @Table(name="solicitudes")
public class Solicitud {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="cliente_id") public Usuario cliente;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="servicio_id") public Servicio servicio;
 @Column(nullable=false,length=700) public String detalle;
 @Column(length=220) public String direccion;
 @Column(name="precio_referencial",nullable=false,precision=10,scale=2) public BigDecimal precioReferencial;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) public EstadoSolicitud estado=EstadoSolicitud.PENDIENTE;
 @Column(name="creado_en",nullable=false) public Instant creadoEn=Instant.now();
}
