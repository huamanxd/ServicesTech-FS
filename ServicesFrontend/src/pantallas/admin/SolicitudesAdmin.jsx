import { useState } from 'react'
import { admin } from '../../api/portal'
import DetalleOrden from '../../componentes/DetalleOrden'
import { Campo, Cargando, EstadoBadge, Mensajes, PrioridadBadge, Vacio } from '../../componentes/Ui'
import { useAccion } from '../../hooks/useAccion'
import { useCarga } from '../../hooks/useCarga'
import { SOLES, etiqueta, fecha, localAIso } from '../../util/formato'

const FILTROS = ['', 'PENDIENTE', 'EN_REVISION', 'APROBADA', 'RECHAZADA', 'CANCELADA']

export default function SolicitudesAdmin() {
  const [filtro, setFiltro] = useState('PENDIENTE')
  const lista = useCarga(() => admin.solicitudes(filtro), [filtro])
  const referencias = useCarga(() => Promise.all([admin.servicios(), admin.usuarios('TECNICO')]), [])
  const accion = useAccion()
  const [generando, setGenerando] = useState(null)

  async function revisar(s, estado) {
    if (await accion.ejecutar(() => admin.revisar(s.id, estado), `Solicitud #${s.id}: ${etiqueta(estado)}.`)) lista.recargar()
  }

  return (
    <section className="space-y-4">
      <div className="flex flex-wrap items-end gap-3">
        <Campo id="filtro-solicitudes" etiqueta="Filtrar por estado">
          <select id="filtro-solicitudes" className="campo" value={filtro} onChange={(e) => setFiltro(e.target.value)}>
            {FILTROS.map((f) => (
              <option key={f} value={f}>
                {f ? etiqueta(f) : 'Todas'}
              </option>
            ))}
          </select>
        </Campo>
      </div>
      <Mensajes error={lista.error || accion.error} exito={accion.exito} />
      {lista.cargando && !lista.datos && <Cargando />}
      {lista.datos?.length === 0 && <Vacio>No hay solicitudes con este filtro.</Vacio>}
      {lista.datos?.map((s) => (
        <article key={s.id} className="tarjeta">
          {s.orden ? (
            <>
              <p className="text-xs texto-suave mb-2">
                Solicitud #{s.id} · {fecha(s.fechaSolicitud)} · ya tiene orden
              </p>
              <DetalleOrden orden={s.orden} />
            </>
          ) : (
            <>
              <div className="flex flex-wrap items-center gap-2">
                <strong>Solicitud #{s.id}</strong>
                <EstadoBadge estado={s.estado} />
                <PrioridadBadge prioridad={s.prioridad} />
              </div>
              <h3 className="font-bold mt-2">{s.asunto}</h3>
              <p className="text-sm text-slate-600 mt-1">{s.descripcion}</p>
              <p className="text-xs texto-suave mt-2">
                {s.cliente.nombre} · {s.servicio} (ref. {SOLES.format(s.precioReferencial)}) · {fecha(s.fechaSolicitud)}
                {s.direccion && ` · ${s.direccion}`}
              </p>
              <div className="flex flex-wrap gap-2 mt-4">
                {s.estado === 'PENDIENTE' && (
                  <button type="button" className="btn btn-claro btn-chico" disabled={accion.ocupado} onClick={() => revisar(s, 'EN_REVISION')}>
                    Marcar en revisión
                  </button>
                )}
                {['PENDIENTE', 'EN_REVISION'].includes(s.estado) && (
                  <>
                    <button type="button" className="btn btn-oscuro btn-chico" disabled={accion.ocupado} onClick={() => revisar(s, 'APROBADA')}>
                      Aprobar
                    </button>
                    <button type="button" className="btn btn-peligro btn-chico" disabled={accion.ocupado} onClick={() => revisar(s, 'RECHAZADA')}>
                      Rechazar
                    </button>
                  </>
                )}
                {s.estado === 'APROBADA' && (
                  <button type="button" className="btn btn-naranja btn-chico" onClick={() => setGenerando(generando === s.id ? null : s.id)}>
                    {generando === s.id ? 'Cerrar formulario' : 'Generar orden'}
                  </button>
                )}
              </div>
              {generando === s.id && (
                <GenerarOrden
                  solicitud={s}
                  servicios={referencias.datos?.[0] ?? []}
                  tecnicos={(referencias.datos?.[1] ?? []).filter((t) => t.activo)}
                  onGenerada={() => {
                    setGenerando(null)
                    lista.recargar()
                  }}
                />
              )}
            </>
          )}
        </article>
      ))}
    </section>
  )
}

function GenerarOrden({ solicitud, servicios, tecnicos, onGenerada }) {
  const [seleccion, setSeleccion] = useState([solicitud.servicioId])
  const [fechaLimite, setFechaLimite] = useState('')
  const [tecnicoId, setTecnicoId] = useState('')
  const [observaciones, setObservaciones] = useState('')
  const accion = useAccion()

  const alternar = (id) => setSeleccion((actual) => (actual.includes(id) ? actual.filter((x) => x !== id) : [...actual, id]))

  async function enviar(e) {
    e.preventDefault()
    if (seleccion.length === 0) {
      accion.setError('Incluye al menos un servicio.')
      return
    }
    const cuerpo = {
      servicioIds: seleccion,
      fechaLimite: localAIso(fechaLimite),
      tecnicoId: tecnicoId ? Number(tecnicoId) : null,
      observaciones: observaciones.trim() || null,
    }
    if (await accion.ejecutar(() => admin.generarOrden(solicitud.id, cuerpo))) onGenerada()
  }

  return (
    <form onSubmit={enviar} className="nota mt-4 space-y-4">
      <strong>Nueva orden para la solicitud #{solicitud.id}</strong>
      <fieldset>
        <legend className="label">Servicios incluidos</legend>
        <div className="grid sm:grid-cols-2 gap-1">
          {servicios
            .filter((s) => s.activo || s.id === solicitud.servicioId)
            .map((s) => (
              <label key={s.id} className="flex items-center gap-2 text-sm">
                <input type="checkbox" checked={seleccion.includes(s.id)} onChange={() => alternar(s.id)} />
                {s.nombre} ({SOLES.format(s.precioReferencial)})
              </label>
            ))}
        </div>
      </fieldset>
      <div className="grid sm:grid-cols-2 gap-4">
        <Campo id={`limite-${solicitud.id}`} etiqueta="Fecha límite" ayuda="Si la dejas vacía se calcula según la prioridad.">
          <input id={`limite-${solicitud.id}`} type="datetime-local" className="campo" value={fechaLimite} onChange={(e) => setFechaLimite(e.target.value)} />
        </Campo>
        <Campo id={`tecnico-${solicitud.id}`} etiqueta="Técnico" ayuda="Opcional: si no eliges, queda en la bandeja para que un técnico la tome.">
          <select id={`tecnico-${solicitud.id}`} className="campo" value={tecnicoId} onChange={(e) => setTecnicoId(e.target.value)}>
            <option value="">Dejar en la bandeja</option>
            {tecnicos.map((t) => (
              <option key={t.id} value={t.id}>
                {t.nombres} {t.apellidos}
              </option>
            ))}
          </select>
        </Campo>
      </div>
      <Campo id={`obs-${solicitud.id}`} etiqueta="Observaciones">
        <input id={`obs-${solicitud.id}`} className="campo" maxLength={200} value={observaciones} onChange={(e) => setObservaciones(e.target.value)} />
      </Campo>
      <Mensajes error={accion.error} />
      <button className="btn btn-oscuro btn-chico" disabled={accion.ocupado}>
        {accion.ocupado ? 'Generando…' : 'Generar orden'}
      </button>
    </form>
  )
}
