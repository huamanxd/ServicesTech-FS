package pe.fullstack.servicesbackend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.fullstack.servicesbackend.portal.dto.Respuestas;
import pe.fullstack.servicesbackend.portal.modelo.Usuario;
import pe.fullstack.servicesbackend.portal.repositorio.UsuarioRepositorio;
import pe.fullstack.servicesbackend.portal.servicio.PortalExcepcion;
import java.util.List;

/**
 * UserController ORIGINAL: ya no usa listas ficticias ni publica contraseñas.
 * Los administradores consultan la tabla usuarios con autenticación obligatoria.
 * El alta pública se hace exclusivamente en POST /api/auth/registro.
 */
@RestController
@RequestMapping("/api/admin/usuarios")
public class UserController {
    private final UsuarioRepositorio usuarios;
    public UserController(UsuarioRepositorio usuarios) { this.usuarios = usuarios; }

    @GetMapping
    public List<Respuestas.Perfil> getUsers() {
        return usuarios.findAll().stream().map(Respuestas.Perfil::de).toList();
    }
    @GetMapping("/{id}")
    public Respuestas.Perfil getUser(@PathVariable Long id) {
        Usuario usuario = usuarios.findById(id)
                .orElseThrow(() -> new PortalExcepcion(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        return Respuestas.Perfil.de(usuario);
    }
}
