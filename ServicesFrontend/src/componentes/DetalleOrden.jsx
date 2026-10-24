import { SOLES, fecha } from '../util/formato'
import { EstadoBadge, PrioridadBadge } from './Ui'

/** Datos comunes de una orden: estado, prioridad, plazos, servicios, solución y calificación. */
export default function DetalleOrden({ orden, mostrarCliente = true }) {
  return (
    <div>
      <div className="flex flex-wrap items-center gap-2">
        <strong>Orden #{orden.id}</strong>
        <EstadoBadge estado={orden.estado} />
        <PrioridadBadge prioridad={orden.prioridad} />
        {orden.retrasada && <span className="estado estado-rojo">Con retraso</span>}
      </div>
      <h3 className="font-bold mt-2">{orden.asunto}</h3>
      <p className="text-sm text-slate-600 mt-1">{orden.descripcion}</p>
      <dl className="datos mt-3">
        {mostrarCliente && (
          <>
            <dt>Cliente</dt>
            <dd>{orden.cliente?.nombre}</dd>
          </>
        )}
        {orden.direccion && (
          <>
            <dt>Dirección</dt>
            <dd>{orden.direccion}</dd>
          </>
        )}
        <dt>Técnico</dt>
        <dd>{orden.tecnico?.nombre ?? 'Sin asignar'}</dd>
        <dt>Creada</dt>
        <dd>{fecha(orden.fechaCreacion)}</dd>
        <dt>Fecha límite</dt>
        <dd>{fecha(orden.fechaLimite)}</dd>
        {orden.fechaCierre && (
          <>
            <dt>Cierre</dt>
            <dd>{fecha(orden.fechaCierre)}</dd>
          </>
        )}
        <dt>Servicios</dt>
        <dd>
          {orden.servicios.map((s) => `${s.nombre} (${SOLES.format(s.precioReferencial)})`).join(', ')} · Total ref.{' '}
          {SOLES.format(orden.total)}
        </dd>
        {orden.observaciones && (
          <>
            <dt>Observaciones</dt>
            <dd>{orden.observaciones}</dd>
          </>
        )}
      </dl>
      {orden.solucion && (
        <div className="nota mt-3">
          <strong className="text-sm">Solución aplicada</strong>
          <p className="text-sm">{orden.solucion.descripcion}</p>
          <span className="text-xs texto-suave">
            {orden.solucion.tecnico?.nombre} · {fecha(orden.solucion.fecha)}
          </span>
        </div>
      )}
      {orden.satisfaccion && (
        <p className="text-sm mt-2">
          Calificación del cliente: <strong>{'★'.repeat(orden.satisfaccion.calificacion)}</strong>
          {orden.satisfaccion.comentario && ` — “${orden.satisfaccion.comentario}”`}
        </p>
      )}
    </div>
  )
}
