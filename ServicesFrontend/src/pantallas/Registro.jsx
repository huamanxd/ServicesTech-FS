import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { auth } from '../api/portal'
import { mensajeError } from '../api/http'
import CamposPersona from '../componentes/CamposPersona'
import { Mensajes } from '../componentes/Ui'
import { useAutenticacion } from '../contexto/Autenticacion'
import { rutaInicio } from '../util/roles'
import { sinErrores, validarRegistro } from '../util/validaciones'

const VACIO = { nombres: '', apellidos: '', dni: '', telefono: '', email: '', clave: '', confirmar: '' }

/** Registro público: siempre crea una cuenta de cliente. */
export default function Registro() {
  const { usuario, entrar } = useAutenticacion()
  const navegar = useNavigate()
  const [form, setForm] = useState(VACIO)
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [ocupado, setOcupado] = useState(false)

  if (usuario) return <Navigate to={rutaInicio(usuario)} replace />

  function cambiar(e) {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: '' })
  }

  async function enviar(e) {
    e.preventDefault()
    const fallas = validarRegistro(form)
    setErrores(fallas)
    setError('')
    if (!sinErrores(fallas)) return
    setOcupado(true)
    try {
      const { confirmar: _confirmar, ...datos } = form
      entrar(await auth.registro({ ...datos, email: datos.email.trim() }))
      navegar('/cliente', { replace: true })
    } catch (err) {
      setError(mensajeError(err))
    } finally {
      setOcupado(false)
    }
  }

  return (
    <main className="contenedor py-14">
      <div className="form-card registro-card">
        <span className="etiqueta">REGISTRO DE CLIENTES</span>
        <h1 className="titulo-seccion mt-2">Crea tu cuenta</h1>
        <p className="texto-apoyo mb-7">Con tu cuenta podrás registrar solicitudes y seguir cada atención.</p>
        <form noValidate className="space-y-4" onSubmit={enviar}>
          <CamposPersona valores={form} errores={errores} onCambio={cambiar} unaColumna />
          <Mensajes error={error} />
          <button type="submit" disabled={ocupado} className="btn btn-oscuro w-full justify-center">
            {ocupado ? 'Creando cuenta…' : 'Crear cuenta de cliente'}
          </button>
        </form>
        <p className="text-sm text-slate-600 text-center mt-6">
          ¿Ya tienes cuenta?{' '}
          <Link className="link" to="/acceso/cliente">
            Inicia sesión
          </Link>
        </p>
      </div>
    </main>
  )
}
