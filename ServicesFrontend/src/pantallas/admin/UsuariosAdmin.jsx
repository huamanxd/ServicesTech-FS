import { useState } from 'react'
import { admin } from '../../api/portal'
import CamposPersona from '../../componentes/CamposPersona'
import { Campo, Cargando, Mensajes } from '../../componentes/Ui'
import { useAccion } from '../../hooks/useAccion'
import { useCarga } from '../../hooks/useCarga'
import { etiqueta, fecha } from '../../util/formato'
import { sinErrores, validarPersonal } from '../../util/validaciones'

const VACIO = { nombres: '', apellidos: '', dni: '', telefono: '', email: '', clave: '', confirmar: '', rol: 'TECNICO' }
const FILTROS = ['', 'CLIENTE', 'TECNICO', 'ADMINISTRADOR']

export default function UsuariosAdmin({ usuarioActual }) {
  const [filtro, setFiltro] = useState('')
  const lista = useCarga(() => admin.usuarios(filtro), [filtro])
  const [form, setForm] = useState(VACIO)
  const [errores, setErrores] = useState({})
  const alta = useAccion()
  const estado = useAccion()

  function cambiar(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: '' })
  }

  async function crear(e) {
    e.preventDefault()
    const fallas = validarPersonal(form)
    setErrores(fallas)
    if (!sinErrores(fallas)) return
    const { confirmar: _confirmar, ...datos } = form
    const creado = await alta.ejecutar(
      () => admin.crearUsuario({ ...datos, email: datos.email.trim() }),
      'Cuenta creada. Ya puede ingresar desde Acceso Personal.',
    )
    if (creado) {
      setForm(VACIO)
      lista.recargar()
    }
  }

  async function alternar(u) {
    const verbo = u.activo ? 'desactivar' : 'activar'
    if (!window.confirm(`¿Seguro que deseas ${verbo} a ${u.nombres} ${u.apellidos}?`)) return
    if (await estado.ejecutar(() => admin.cambiarActivo(u.id, !u.activo), `Usuario ${u.activo ? 'desactivado' : 'activado'}.`)) lista.recargar()
  }

  return (
    <div className="grid xl:grid-cols-[1fr_1.2fr] gap-6 items-start">
      <section className="tarjeta">
        <h2 className="subtitulo">Registrar técnico o administrador</h2>
        <p className="text-slate-600 text-sm my-3">Solo un administrador puede crear cuentas de personal. Los clientes se registran solos.</p>
        <form onSubmit={crear} noValidate className="space-y-4">
          <Campo id="p-rol" etiqueta="Rol *" error={errores.rol}>
            <select id="p-rol" name="rol" className="campo" value={form.rol} onChange={cambiar}>
              <option value="TECNICO">Técnico</option>
              <option value="ADMINISTRADOR">Administrador</option>
            </select>
          </Campo>
          <CamposPersona valores={form} errores={errores} onCambio={cambiar} prefijo="p-" />
          <Mensajes error={alta.error} exito={alta.exito} />
          <button className="btn btn-oscuro" disabled={alta.ocupado}>
            {alta.ocupado ? 'Creando…' : 'Crear cuenta'}
          </button>
        </form>
      </section>

      <section className="tarjeta">
        <div className="flex flex-wrap items-end justify-between gap-3 mb-4">
          <h2 className="subtitulo">Usuarios ({lista.datos?.length ?? 0})</h2>
          <Campo id="filtro-usuarios" etiqueta="Rol">
            <select id="filtro-usuarios" className="campo" value={filtro} onChange={(e) => setFiltro(e.target.value)}>
              {FILTROS.map((f) => (
                <option key={f} value={f}>
                  {f ? etiqueta(f) : 'Todos'}
                </option>
              ))}
            </select>
          </Campo>
        </div>
        <p className="text-xs texto-suave mb-3">Los usuarios no se eliminan: se desactivan y pierden el acceso de inmediato.</p>
        <Mensajes error={lista.error || estado.error} exito={estado.exito} />
        {lista.cargando && !lista.datos && <Cargando />}
        <ul className="space-y-2">
          {lista.datos?.map((u) => (
            <li key={u.id} className="registro flex flex-wrap justify-between items-center gap-3">
              <div>
                <strong>
                  {u.nombres} {u.apellidos}
                </strong>
                <p className="text-xs texto-suave">
                  {u.email} · {u.roles.map(etiqueta).join(', ')} · desde {fecha(u.fechaRegistro)}
                </p>
              </div>
              <div className="flex items-center gap-2">
                <span className={`estado ${u.activo ? 'estado-verde' : 'estado-rojo'}`}>{u.activo ? 'Activo' : 'Inactivo'}</span>
                {u.id !== usuarioActual.id && (
                  <button type="button" className={`btn btn-chico ${u.activo ? 'btn-peligro' : 'btn-claro'}`} disabled={estado.ocupado} onClick={() => alternar(u)}>
                    {u.activo ? 'Desactivar' : 'Activar'}
                  </button>
                )}
              </div>
            </li>
          ))}
        </ul>
      </section>
    </div>
  )
}
