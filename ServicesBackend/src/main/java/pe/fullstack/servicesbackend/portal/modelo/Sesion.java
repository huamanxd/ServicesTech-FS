package pe.fullstack.servicesbackend.portal.modelo;
import jakarta.persistence.*;
import java.time.Instant;
@Entity @Table(name="sesiones")
public class Sesion {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
 @ManyToOne(optional=false,fetch=FetchType.EAGER) @JoinColumn(name="usuario_id") public Usuario usuario;
 @Column(name="token_hash",nullable=false,unique=true,length=64) public String tokenHash;
 @Column(name="expira_en",nullable=false) public Instant expiraEn;
}
