package pe.fullstack.servicesbackend.portal.modelo;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="usuarios")
public class Usuario {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @Column(nullable=false,length=90) public String nombres;
 @Column(nullable=false,length=90) public String apellidos;
 @Column(nullable=false,length=180) public String email;
 @Column(length=15) public String telefono;
 @Column(name="clave_hash",nullable=false,length=100) public String claveHash;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=15) public Rol rol;
 @Column(nullable=false) public boolean activo=true;
 @Column(name="creado_en",nullable=false) public Instant creadoEn=Instant.now();
}
