import { admin } from '../../api/portal'
import { BarrasConteo, Cargando, Indicador, Mensajes, PrioridadBadge, Vacio } from '../../componentes/Ui'
import { useCarga } from '../../hooks/useCarga'
import { etiqueta, fecha, horas, porcentaje, promedio } from '../../util/formato'

export default function DashboardAdmin() {
  const { datos, cargando, error, recargar } = useCarga(() => admin.dashboard(), [])
  if (cargando && !datos) return <Cargando />
  if (!datos) return <Mensajes error={error} />

  const i = datos.indicadores
  const servicios = Object.fromEntries(datos.serviciosMasSolicitados.map((c) => [c.nombre, c.cantidad]))
  const estrellas = Object.fromEntries(Object.entries(datos.calificaciones).map(([n, c]) => [`${n} ★`, c]))

  return (
    <div className="space-y-6">
      <div className="flex justify-end">
        <button type="button" className="btn btn-claro btn-chico" onClick={recargar} disabled={cargando}>
          {cargando ? 'Actualizando…' : 'Actualizar'}
        </button>
      </div>
      <Mensajes error={error} />
      <div className="grid-indicadores">
        <Indicador titulo="Solicitudes por revisar" valor={datos.solicitudesPorRevisar} detalle={`${datos.solicitudes} en total`} />
        <Indicador titulo="Aprobadas sin orden" valor={datos.aprobadasSinOrden} />
        <Indicador titulo="Órdenes abiertas" valor={i.abiertas} detalle={`${i.ordenes} en total`} />
        <Indicador titulo="Órdenes con retraso" valor={i.retrasadas} tono={i.retrasadas ? 'tono-alerta' : ''} />
        <Indicador titulo="Tiempo promedio de atención" valor={horas(i.tiempoPromedioHoras)} detalle="creación → solución" />
        <Indicador titulo="Cumplimiento de plazos" valor={porcentaje(i.cumplimientoPct)} detalle={`${i.resueltas} resueltas`} />
        <Indicador titulo="Satisfacción promedio" valor={promedio(i.satisfaccionPromedio)} detalle={`${i.evaluaciones} evaluaciones`} />
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <BarrasConteo titulo="Órdenes por estado" conteos={datos.ordenesPorEstado} />
        <BarrasConteo titulo="Solicitudes por estado" conteos={datos.solicitudesPorEstado} />
        <BarrasConteo titulo="Servicios más solicitados" conteos={servicios} etiquetar={(n) => n} />
        <BarrasConteo titulo="Distribución de calificaciones" conteos={estrellas} etiquetar={(n) => n} />
      </div>

      <section className="tarjeta">
        <h3 className="subtitulo">Órdenes retrasadas sin resolver</h3>
        {datos.retrasadas.length === 0 ? (
          <Vacio>No hay órdenes retrasadas. 🎉</Vacio>
        ) : (
          <div className="tabla-envoltura">
            <table className="tabla">
              <thead>
                <tr>
                  <th>Orden</th>
                  <th>Asunto</th>
                  <th>Prioridad</th>
                  <th>Estado</th>
                  <th>Técnico</th>
                  <th>Límite</th>
                  <th>Retraso</th>
                </tr>
              </thead>
              <tbody>
                {datos.retrasadas.map((o) => (
                  <tr key={o.id}>
                    <td>#{o.id}</td>
                    <td>{o.asunto}</td>
                    <td>
                      <PrioridadBadge prioridad={o.prioridad} />
                    </td>
                    <td>{etiqueta(o.estado)}</td>
                    <td>{o.tecnico?.nombre ?? 'Sin asignar'}</td>
                    <td>{fecha(o.fechaLimite)}</td>
                    <td>{horas(o.horasRetraso)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <section className="tarjeta">
        <h3 className="subtitulo">Rendimiento por técnico</h3>
        {datos.tecnicos.length === 0 ? (
          <Vacio>Aún no hay técnicos registrados. Créalos en la pestaña Usuarios.</Vacio>
        ) : (
          <div className="tabla-envoltura">
            <table className="tabla">
              <thead>
                <tr>
                  <th>Técnico</th>
                  <th>Asignadas</th>
                  <th>En curso</th>
                  <th>Resueltas</th>
                  <th>Retrasadas</th>
                  <th>Tiempo prom.</th>
                  <th>Cumplimiento</th>
                  <th>Satisfacción</th>
                </tr>
              </thead>
              <tbody>
                {datos.tecnicos.map((t) => (
                  <tr key={t.tecnico.id}>
                    <td>
                      {t.tecnico.nombre}
                      {!t.activo && <span className="texto-suave text-xs"> (inactivo)</span>}
                    </td>
                    <td>{t.asignadas}</td>
                    <td>{t.enCurso}</td>
                    <td>{t.resueltas}</td>
                    <td>{t.retrasadas}</td>
                    <td>{horas(t.tiempoPromedioHoras)}</td>
                    <td>{porcentaje(t.cumplimientoPct)}</td>
                    <td>{promedio(t.satisfaccionPromedio)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </div>
  )
}
