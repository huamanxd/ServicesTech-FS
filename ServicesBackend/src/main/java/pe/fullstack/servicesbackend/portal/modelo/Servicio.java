package pe.fullstack.servicesbackend.portal.modelo;
import jakarta.persistence.*;
import java.math.BigDecimal;
@Entity @Table(name="servicios")
public class Servicio {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,unique=true,length=45) public String codigo;
 @Column(nullable=false,length=120) public String nombre;
 @Column(nullable=false,length=600) public String descripcion;
 @Column(nullable=false,length=25) public String categoria;
 @Column(name="precio_referencial",nullable=false,precision=10,scale=2) public BigDecimal precioReferencial;
 @Column(name="duracion_minutos",nullable=false) public int duracionMinutos;
 @Column(nullable=false) public boolean activo=true;
}
