package pe.fullstack.servicesbackend.servicio;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.fullstack.servicesbackend.dto.Peticiones;
import pe.fullstack.servicesbackend.dto.Respuestas.PrioridadVista;
import pe.fullstack.servicesbackend.dto.Respuestas.ServicioVista;
import pe.fullstack.servicesbackend.dto.VistaMapper;
import pe.fullstack.servicesbackend.modelo.*;
import pe.fullstack.servicesbackend.repositorio.*;

import java.util.List;

/** Servicios del catálogo, prioridades y estados (tablas de referencia del ERD). */
@Service
@RequiredArgsConstructor
public class CatalogoServicio {

    private final ServicioRepositorio servicios;
    private final PrioridadRepositorio prioridades;
    private final EstadoSolicitudRepositorio estadosSolicitud;
    private final EstadoOrdenRepositorio estadosOrden;
    private final RolRepositorio roles;
    private final VistaMapper mapper;

    @Transactional(readOnly = true)
    public List<ServicioVista> serviciosActivos() {
        return mapper.servicios(servicios.findByActivoTrueOrderByIdAsc());
    }

    @Transactional(readOnly = true)
    public List<ServicioVista> todosLosServicios() {
        return mapper.servicios(servicios.findAllByOrderByIdAsc());
    }

    @Transactional(readOnly = true)
    public List<PrioridadVista> prioridades() {
        return mapper.prioridades(prioridades.findAllByOrderByIdAsc());
    }

    @Transactional
    public ServicioVista crearServicio(Peticiones.ServicioForm f) {
        if (servicios.existsByCodigoIgnoreCase(f.codigo())) {
            throw NegocioExcepcion.conflicto("El código " + f.codigo() + " ya está registrado");
        }
        Servicio s = new Servicio();
        copiar(s, f);
        return mapper.servicio(servicios.save(s));
    }

    @Transactional
    public ServicioVista editarServicio(Long id, Peticiones.ServicioForm f) {
        Servicio s = servicio(id);
        if (!s.getCodigo().equalsIgnoreCase(f.codigo()) && servicios.existsByCodigoIgnoreCase(f.codigo())) {
            throw NegocioExcepcion.conflicto("El código " + f.codigo() + " ya está registrado");
        }
        copiar(s, f);
        return mapper.servicio(s);
    }

    /** Baja lógica: el servicio deja de ofrecerse pero conserva su historial. */
    @Transactional
    public void desactivarServicio(Long id) {
        servicio(id).setActivo(false);
    }

    public Servicio servicio(Long id) {
        return servicios.findById(id).orElseThrow(() -> NegocioExcepcion.noEncontrado("Servicio no encontrado"));
    }

    public Prioridad prioridad(Long id) {
        return prioridades.findById(id).orElseThrow(() -> NegocioExcepcion.noEncontrado("Prioridad no encontrada"));
    }

    public EstadoSolicitud estado(EstadoSolicitud.Codigo codigo) {
        return estadosSolicitud.findByNombre(codigo).orElseThrow(() -> new IllegalStateException("Falta el estado " + codigo));
    }

    public EstadoOrden estado(EstadoOrden.Codigo codigo) {
        return estadosOrden.findByNombre(codigo).orElseThrow(() -> new IllegalStateException("Falta el estado " + codigo));
    }

    public Rol rol(Rol.Codigo codigo) {
        return roles.findByNombre(codigo).orElseThrow(() -> new IllegalStateException("Falta el rol " + codigo));
    }

    private static void copiar(Servicio s, Peticiones.ServicioForm f) {
        s.setCodigo(f.codigo());
        s.setNombre(f.nombre().strip());
        s.setDescripcion(f.descripcion().strip());
        s.setCategoria(f.categoria());
        s.setPrecioReferencial(f.precioReferencial());
        s.setDuracionMinutos(f.duracionMinutos());
        s.setActivo(f.activo());
    }
}
