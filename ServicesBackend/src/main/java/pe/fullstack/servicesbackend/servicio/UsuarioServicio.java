package pe.fullstack.servicesbackend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas.Perfil;
import pe.fullstack.servicesbackend.dto.VistaMapper;
import pe.fullstack.servicesbackend.modelo.Rol;
import pe.fullstack.servicesbackend.modelo.Usuario;
import pe.fullstack.servicesbackend.repositorio.OrdenRepositorio;
import pe.fullstack.servicesbackend.repositorio.UsuarioRepositorio;

import java.util.List;

/** Gestión de usuarios por el administrador. Los usuarios nunca se eliminan. */
@Service
@RequiredArgsConstructor
public class UsuarioServicio {

    private final UsuarioRepositorio usuarios;
    private final OrdenRepositorio ordenes;
    private final IdentidadServicio identidad;
    private final VistaMapper mapper;

    @Transactional(readOnly = true)
    public List<Perfil> listar(Rol.Codigo rol) {
        return mapper.perfiles(rol == null ? usuarios.findAllByOrderByIdAsc() : usuarios.buscarPorRol(rol));
    }

    @Transactional(readOnly = true)
    public Perfil obtener(Long id) {
        return mapper.perfil(usuario(id));
    }

    /** Solo un administrador puede crear técnicos u otros administradores. */
    @Transactional
    public Perfil crearPersonal(Peticiones.NuevoPersonal p) {
        Usuario u = identidad.crearUsuario(p.nombres(), p.apellidos(), p.dni(), p.email(), p.telefono(), p.clave(),
                Rol.Codigo.valueOf(p.rol()));
        return mapper.perfil(u);
    }

    @Transactional
    public Perfil cambiarActivo(Long id, boolean activo, Long actorId) {
        Usuario u = usuario(id);
        if (u.isActivo() == activo) return mapper.perfil(u);
        if (!activo) {
            if (u.getId().equals(actorId)) {
                throw NegocioExcepcion.conflicto("No puedes desactivar tu propia cuenta");
            }
            if (u.tieneRol(Rol.Codigo.ADMINISTRADOR) && usuarios.contarActivosPorRol(Rol.Codigo.ADMINISTRADOR) <= 1) {
                throw NegocioExcepcion.conflicto("Debe quedar al menos un administrador activo");
            }
            if (u.tieneRol(Rol.Codigo.TECNICO) && ordenes.asignacionesVigentes(u.getId()) > 0) {
                throw NegocioExcepcion.conflicto("El técnico tiene órdenes asignadas: reasígnalas antes de desactivarlo");
            }
            identidad.revocarSesiones(u.getId());
        }
        u.setActivo(activo);
        return mapper.perfil(u);
    }

    Usuario usuario(Long id) {
        return usuarios.findById(id).orElseThrow(() -> NegocioExcepcion.noEncontrado("Usuario no encontrado"));
    }

    /** Técnico activo, para asignarle órdenes. */
    Usuario tecnicoActivo(Long id) {
        Usuario u = usuario(id);
        if (!u.tieneRol(Rol.Codigo.TECNICO)) throw NegocioExcepcion.invalido("El usuario seleccionado no es técnico");
        if (!u.isActivo()) throw NegocioExcepcion.conflicto("El técnico está desactivado");
        return u;
    }
}
