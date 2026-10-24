import { useState } from 'react'
import { cliente, publico } from '../../api/portal'
import { Campo, Cargando, Mensajes } from '../../componentes/Ui'
import { useAccion } from '../../hooks/useAccion'
import { useCarga } from '../../hooks/useCarga'
import { SOLES, etiqueta } from '../../util/formato'
import { sinErrores, validarSolicitud } from '../../util/validaciones'

export default function NuevaSolicitud({ servicioInicial, onCreada }) {
  const referencias = useCarga(() => Promise.all([publico.servicios(), publico.prioridades()]), [])
  const [form, setForm] = useState({ servicioId: servicioInicial, prioridadId: '', asunto: '', descripcion: '', direccion: '' })
  const [errores, setErrores] = useState({})
  const accion = useAccion()

  const [servicios, prioridades] = referencias.datos ?? [[], []]
  const prioridadElegida = form.prioridadId || prioridades.find((p) => p.nombre === 'MEDIA')?.id || ''

  const cambiar = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: '' })
  }

  async function enviar(e) {
    e.preventDefault()
    const datos = { ...form, prioridadId: prioridadElegida }
    const fallas = validarSolicitud(datos)
    setErrores(fallas)
    if (!sinErrores(fallas)) return
    const creada = await accion.ejecutar(() =>
      cliente.registrar({
        servicioId: Number(datos.servicioId),
        prioridadId: Number(datos.prioridadId),
        asunto: datos.asunto.trim(),
        descripcion: datos.descripcion.trim(),
        direccion: datos.direccion.trim() || null,
      }),
    )
    if (creada) onCreada(creada)
  }

  if (referencias.cargando) return <Cargando />
  return (
    <section className="tarjeta max-w-2xl">
      <h2 className="subtitulo">Nueva solicitud de servicio</h2>
      <p className="text-sm text-slate-600 mb-5">Registrar una solicitud no implica pago ni confirma una cita: la revisaremos y te avisaremos el avance aquí.</p>
      <Mensajes error={referencias.error || accion.error} />
      <form onSubmit={enviar} noValidate className="space-y-4">
        <div className="grid sm:grid-cols-2 gap-4">
          <Campo id="servicioId" etiqueta="Servicio *" error={errores.servicioId}>
            <select className="campo" id="servicioId" name="servicioId" value={form.servicioId} onChange={cambiar}>
              <option value="">Selecciona una opción</option>
              {servicios.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.nombre} — {SOLES.format(s.precioReferencial)}
                </option>
              ))}
            </select>
          </Campo>
          <Campo id="prioridadId" etiqueta="Prioridad *" error={errores.prioridadId}>
            <select className="campo" id="prioridadId" name="prioridadId" value={prioridadElegida} onChange={cambiar}>
              {prioridades.map((p) => (
                <option key={p.id} value={p.id}>
                  {etiqueta(p.nombre)} — {p.descripcion}
                </option>
              ))}
            </select>
          </Campo>
        </div>
        <Campo id="asunto" etiqueta="Asunto *" error={errores.asunto}>
          <input className="campo" id="asunto" name="asunto" maxLength={150} value={form.asunto} onChange={cambiar} placeholder="Ej.: La laptop no enciende" />
        </Campo>
        <Campo id="descripcion" etiqueta="¿Qué sucede? *" error={errores.descripcion}>
          <textarea
            className="campo min-h-28"
            id="descripcion"
            name="descripcion"
            maxLength={700}
            value={form.descripcion}
            onChange={cambiar}
            placeholder="Cuéntanos el problema, desde cuándo ocurre y el modelo del equipo…"
          />
        </Campo>
        <Campo id="direccion" etiqueta="Dirección o distrito (opcional)" error={errores.direccion}>
          <input className="campo" id="direccion" name="direccion" maxLength={220} value={form.direccion} onChange={cambiar} />
        </Campo>
        <button className="btn btn-oscuro w-full justify-center" disabled={accion.ocupado}>
          {accion.ocupado ? 'Registrando…' : 'Registrar solicitud'}
        </button>
      </form>
    </section>
  )
}
