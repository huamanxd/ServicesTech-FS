import { useState } from 'react'
import { admin } from '../../api/portal'
import { Campo, Cargando, Mensajes } from '../../componentes/Ui'
import { useAccion } from '../../hooks/useAccion'
import { useCarga } from '../../hooks/useCarga'
import { SOLES } from '../../util/formato'
import { sinErrores, validarServicio } from '../../util/validaciones'

const CATEGORIAS = ['EQUIPOS', 'SOFTWARE', 'REDES', 'SEGURIDAD', 'EMPRESAS']
const VACIO = { codigo: '', nombre: '', descripcion: '', categoria: 'EQUIPOS', precioReferencial: '', duracionMinutos: 60, activo: true }

export default function CatalogoAdmin() {
  const lista = useCarga(() => admin.servicios(), [])
  const [form, setForm] = useState(VACIO)
  const [editando, setEditando] = useState(null)
  const [errores, setErrores] = useState({})
  const accion = useAccion()

  const cambiar = (campo, valor) => {
    setForm({ ...form, [campo]: valor })
    setErrores({ ...errores, [campo]: '' })
  }

  function editar(s) {
    setForm({ ...s, precioReferencial: String(s.precioReferencial) })
    setEditando(s.id)
    setErrores({})
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function limpiar() {
    setForm(VACIO)
    setEditando(null)
    setErrores({})
  }

  async function guardar(e) {
    e.preventDefault()
    const fallas = validarServicio(form)
    setErrores(fallas)
    if (!sinErrores(fallas)) return
    const cuerpo = {
      codigo: form.codigo,
      nombre: form.nombre,
      descripcion: form.descripcion,
      categoria: form.categoria,
      precioReferencial: Number(form.precioReferencial),
      duracionMinutos: Number(form.duracionMinutos),
      activo: form.activo,
    }
    const hecho = await accion.ejecutar(
      () => (editando ? admin.editarServicio(editando, cuerpo) : admin.crearServicio(cuerpo)),
      'Servicio guardado.',
    )
    if (hecho) {
      limpiar()
      lista.recargar()
    }
  }

  async function desactivar(s) {
    if (!window.confirm(`¿Desactivar ${s.nombre}? Dejará de mostrarse en el catálogo.`)) return
    if ((await accion.ejecutar(() => admin.desactivarServicio(s.id), 'Servicio desactivado.')) !== undefined) lista.recargar()
  }

  return (
    <div className="grid lg:grid-cols-[0.9fr_1.1fr] gap-6 items-start">
      <section className="tarjeta">
        <h2 className="subtitulo mb-4">{editando ? 'Editar servicio' : 'Nuevo servicio'}</h2>
        <form onSubmit={guardar} noValidate className="space-y-4">
          <Campo id="codigo" etiqueta="Código" error={errores.codigo}>
            <input id="codigo" className="campo" maxLength={45} value={form.codigo} onChange={(e) => cambiar('codigo', e.target.value.toUpperCase())} />
          </Campo>
          <Campo id="nombre" etiqueta="Nombre" error={errores.nombre}>
            <input id="nombre" className="campo" maxLength={120} value={form.nombre} onChange={(e) => cambiar('nombre', e.target.value)} />
          </Campo>
          <Campo id="descripcion-servicio" etiqueta="Descripción" error={errores.descripcion}>
            <textarea id="descripcion-servicio" className="campo min-h-24" maxLength={600} value={form.descripcion} onChange={(e) => cambiar('descripcion', e.target.value)} />
          </Campo>
          <Campo id="categoria-servicio" etiqueta="Categoría">
            <select id="categoria-servicio" className="campo" value={form.categoria} onChange={(e) => cambiar('categoria', e.target.value)}>
              {CATEGORIAS.map((c) => (
                <option key={c}>{c}</option>
              ))}
            </select>
          </Campo>
          <div className="grid grid-cols-2 gap-3">
            <Campo id="precio" etiqueta="Precio ref. S/" error={errores.precioReferencial}>
              <input id="precio" className="campo" type="number" min="0" step="0.01" value={form.precioReferencial} onChange={(e) => cambiar('precioReferencial', e.target.value)} />
            </Campo>
            <Campo id="duracion" etiqueta="Minutos" error={errores.duracionMinutos}>
              <input id="duracion" className="campo" type="number" min="15" max="10080" value={form.duracionMinutos} onChange={(e) => cambiar('duracionMinutos', e.target.value)} />
            </Campo>
          </div>
          <label className="flex items-center gap-3">
            <input type="checkbox" checked={form.activo} onChange={(e) => cambiar('activo', e.target.checked)} />
            Servicio activo
          </label>
          <Mensajes error={accion.error} exito={accion.exito} />
          <div className="flex gap-2">
            <button className="btn btn-oscuro" disabled={accion.ocupado}>
              {accion.ocupado ? 'Guardando…' : 'Guardar servicio'}
            </button>
            {editando && (
              <button type="button" className="btn btn-claro" onClick={limpiar}>
                Cancelar
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="tarjeta">
        <h2 className="subtitulo mb-4">Servicios ({lista.datos?.length ?? 0})</h2>
        <Mensajes error={lista.error} />
        {lista.cargando && !lista.datos && <Cargando />}
        <div className="space-y-3">
          {lista.datos?.map((s) => (
            <article key={s.id} className="registro flex justify-between gap-3 flex-wrap">
              <div>
                <strong>{s.nombre}</strong>
                <p className="text-xs text-slate-500">
                  {s.codigo} · {s.categoria} · {s.activo ? 'Activo' : 'Inactivo'}
                </p>
                <p className="text-sm text-teal-700 font-semibold">{SOLES.format(s.precioReferencial)}</p>
              </div>
              <div className="flex items-center gap-2">
                <button type="button" className="btn btn-claro btn-chico" onClick={() => editar(s)}>
                  Editar
                </button>
                {s.activo && (
                  <button type="button" className="btn btn-peligro btn-chico" onClick={() => desactivar(s)}>
                    Desactivar
                  </button>
                )}
              </div>
            </article>
          ))}
        </div>
      </section>
    </div>
  )
}
