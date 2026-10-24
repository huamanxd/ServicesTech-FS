import { useState } from 'react'
import { cliente } from '../../api/portal'
import DetalleOrden from '../../componentes/DetalleOrden'
import Historial from '../../componentes/Historial'
import { Cargando, EstadoBadge, Mensajes, PrioridadBadge, Vacio } from '../../componentes/Ui'
import { useAccion } from '../../hooks/useAccion'
import { SOLES, estadoAtencion, fecha } from '../../util/formato'

const CANCELABLES = ['PENDIENTE', 'EN_REVISION', 'APROBADA']

export default function MisSolicitudes({ estado, onCambio }) {
  const { datos, cargando, error } = estado
  const accion = useAccion()

  async function cancelar(s) {
    if (!window.confirm(`¿Cancelar la solicitud #${s.id}?`)) return
    if (await accion.ejecutar(() => cliente.cancelar(s.id), `Solicitud #${s.id} cancelada.`)) onCambio()
  }

  if (cargando && !datos) return <Cargando />
  return (
    <section className="space-y-4">
      <Mensajes error={error || accion.error} exito={accion.exito} />
      {datos?.length === 0 && <Vacio>Aún no tienes solicitudes. Registra la primera desde “Nueva solicitud”.</Vacio>}
      {datos?.map((s) => (
        <article className="tarjeta" key={s.id}>
          {s.orden ? (
            <>
              <p className="text-xs texto-suave mb-2">
                Solicitud #{s.id} del {fecha(s.fechaSolicitud)}
              </p>
              <DetalleOrden orden={s.orden} mostrarCliente={false} />
              <Historial cargar={() => cliente.historial(s.orden.id)} />
              {['RESUELTA', 'CERRADA'].includes(s.orden.estado) && !s.orden.satisfaccion && (
                <Calificar ordenId={s.orden.id} onHecho={onCambio} />
              )}
            </>
          ) : (
            <>
              <div className="flex flex-wrap items-center gap-2">
                <strong>Solicitud #{s.id}</strong>
                <EstadoBadge estado={estadoAtencion(s)} />
                <PrioridadBadge prioridad={s.prioridad} />
              </div>
              <h3 className="font-bold mt-2">{s.asunto}</h3>
              <p className="text-sm text-slate-600 mt-1">{s.descripcion}</p>
              <p className="text-xs texto-suave mt-2">
                {s.servicio} · Ref. {SOLES.format(s.precioReferencial)} · {fecha(s.fechaSolicitud)}
              </p>
              {s.estado === 'APROBADA' && <p className="text-sm mt-2">Aprobada: en breve se generará tu orden de servicio.</p>}
              {CANCELABLES.includes(s.estado) && (
                <button type="button" className="btn btn-peligro btn-chico mt-3" onClick={() => cancelar(s)} disabled={accion.ocupado}>
                  Cancelar solicitud
                </button>
              )}
            </>
          )}
        </article>
      ))}
    </section>
  )
}

function Calificar({ ordenId, onHecho }) {
  const [calificacion, setCalificacion] = useState(0)
  const [comentario, setComentario] = useState('')
  const accion = useAccion()

  async function enviar(e) {
    e.preventDefault()
    if (!calificacion) {
      accion.setError('Elige de 1 a 5 estrellas.')
      return
    }
    if (await accion.ejecutar(() => cliente.calificar(ordenId, { calificacion, comentario: comentario.trim() || null }))) onHecho()
  }

  return (
    <form onSubmit={enviar} className="nota mt-4 space-y-3">
      <strong className="text-sm">¿Cómo calificarías el servicio?</strong>
      <div className="flex gap-1" role="radiogroup" aria-label="Calificación de 1 a 5">
        {[1, 2, 3, 4, 5].map((n) => (
          <button
            key={n}
            type="button"
            role="radio"
            aria-checked={calificacion === n}
            aria-label={`${n} estrella${n > 1 ? 's' : ''}`}
            className={`estrella ${n <= calificacion ? 'activa' : ''}`}
            onClick={() => setCalificacion(n)}
          >
            ★
          </button>
        ))}
      </div>
      <label className="sr-only" htmlFor={`comentario-${ordenId}`}>
        Comentario
      </label>
      <input
        id={`comentario-${ordenId}`}
        className="campo"
        maxLength={200}
        placeholder="Comentario (opcional)"
        value={comentario}
        onChange={(e) => setComentario(e.target.value)}
      />
      <Mensajes error={accion.error} />
      <button className="btn btn-oscuro btn-chico" disabled={accion.ocupado}>
        Enviar calificación
      </button>
    </form>
  )
}
