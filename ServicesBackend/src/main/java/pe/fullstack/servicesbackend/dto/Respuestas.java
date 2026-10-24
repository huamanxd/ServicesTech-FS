package pe.fullstack.servicesbackend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Vistas que devuelve la API (nunca se exponen entidades ni hashes de contraseña). */
public final class Respuestas {

    private Respuestas() {
    }

    public record UsuarioResumen(Long id, String nombre) {
    }

    public record Perfil(Long id, String nombres, String apellidos, String dni, String email, String telefono,
                         List<String> roles, boolean activo, Instant fechaRegistro) {
    }

    public record Acceso(String token, Instant expiraEn, Perfil usuario) {
    }

    public record ServicioVista(Long id, String codigo, String nombre, String descripcion, String categoria,
                                BigDecimal precioReferencial, int duracionMinutos, boolean activo) {
    }

    public record PrioridadVista(Long id, String nombre, String descripcion, int horasObjetivo) {
    }

    public record DetalleVista(Long servicioId, String nombre, BigDecimal precioReferencial) {
    }

    public record SolucionVista(String descripcion, Instant fecha, UsuarioResumen tecnico) {
    }

    public record SatisfaccionVista(int calificacion, String comentario, Instant fecha) {
    }

    public record HistorialVista(String estado, Instant fecha, UsuarioResumen usuario, String observacion) {
    }

    public record OrdenVista(Long id, Long solicitudId, String asunto, String descripcion, String direccion,
                             String prioridad, UsuarioResumen cliente, String estado, Instant fechaCreacion,
                             Instant fechaLimite, Instant fechaCierre, String observaciones, boolean retrasada,
                             UsuarioResumen tecnico, List<DetalleVista> servicios, BigDecimal total,
                             SolucionVista solucion, SatisfaccionVista satisfaccion) {
    }

    public record SolicitudVista(Long id, String asunto, String descripcion, String direccion, String estado,
                                 String prioridad, Long servicioId, String servicio, BigDecimal precioReferencial,
                                 Instant fechaSolicitud, UsuarioResumen cliente, OrdenVista orden) {
    }

    // ---------------------------------------------------------------- dashboards

    /** Indicadores comunes de tiempos, cumplimiento y satisfacción. */
    public record Indicadores(long ordenes, long abiertas, long resueltas, long retrasadas,
                              Double tiempoPromedioHoras, Double cumplimientoPct,
                              Double satisfaccionPromedio, long evaluaciones) {
    }

    public record OrdenRetrasada(Long id, String asunto, String prioridad, String estado, Instant fechaLimite,
                                 UsuarioResumen tecnico, double horasRetraso) {
    }

    public record RendimientoTecnico(UsuarioResumen tecnico, boolean activo, long asignadas, long enCurso,
                                     long resueltas, long retrasadas, Double tiempoPromedioHoras,
                                     Double cumplimientoPct, Double satisfaccionPromedio) {
    }

    public record Conteo(String nombre, long cantidad) {
    }

    public record DashboardAdmin(Indicadores indicadores, long solicitudes, long solicitudesPorRevisar,
                                 long aprobadasSinOrden, Map<String, Long> solicitudesPorEstado,
                                 Map<String, Long> ordenesPorEstado, List<OrdenRetrasada> retrasadas,
                                 List<RendimientoTecnico> tecnicos, List<Conteo> serviciosMasSolicitados,
                                 Map<Integer, Long> calificaciones) {
    }

    public record DashboardCliente(Indicadores indicadores, long solicitudes, Map<String, Long> porEstado,
                                   long pendientesDeCalificar) {
    }

    public record DashboardTecnico(Indicadores indicadores, long porIniciar, long enAtencion,
                                   long disponiblesEnBandeja) {
    }
}
