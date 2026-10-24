package pe.fullstack.servicesbackend.modelo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/** Tabla "usuarios". Nunca se elimina: se activa o desactiva. */
@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @Column(nullable = false, length = 90)
    private String nombres;

    @Column(nullable = false, length = 90)
    private String apellidos;

    @Column(length = 8, unique = true)
    private String dni;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 15)
    private String telefono;

    @Column(name = "clave_hash", nullable = false, length = 100)
    private String claveHash;

    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false)
    private Instant fechaRegistro = Instant.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_rol",
            joinColumns = @JoinColumn(name = "id_usuario"),
            inverseJoinColumns = @JoinColumn(name = "id_rol"))
    private Set<Rol> roles = new HashSet<>();

    public boolean tieneRol(Rol.Codigo codigo) {
        return roles.stream().anyMatch(r -> r.getNombre() == codigo);
    }

    public String nombreCompleto() {
        return nombres + " " + apellidos;
    }
}
