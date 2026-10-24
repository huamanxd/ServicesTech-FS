import { useState } from 'react'
import { Link, Navigate, useLocation, useNavigate, useParams } from 'react-router-dom'
import { auth } from '../api/portal'
import { mensajeError } from '../api/http'
import { Campo, Mensajes } from '../componentes/Ui'
import { useAutenticacion } from '../contexto/Autenticacion'
import { rutaInicio } from '../util/roles'
import { sinErrores, validarAcceso } from '../util/validaciones'

/** Inicio de sesión. /acceso/cliente (Portal Cliente) y /acceso/personal (técnicos y administradores). */
export default function Acceso() {
  const { tipo } = useParams()
  const esPersonal = tipo === 'personal'
  const { usuario, entrar, aviso } = useAutenticacion()
  const navegar = useNavigate()
  const { state } = useLocation()
  const [form, setForm] = useState({ email: '', clave: '' })
  const [errores, setErrores] = useState({})
  const [error, setError] = useState('')
  const [ocupado, setOcupado] = useState(false)

  if (tipo !== 'cliente' && tipo !== 'personal') return <Navigate to="/acceso/cliente" replace />
  if (usuario) return <Navigate replace to={rutaInicio(usuario)} />

  const cambiar = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value })
    setErrores({ ...errores, [e.target.name]: '' })
  }

  async function enviar(e) {
    e.preventDefault()
    const fallas = validarAcceso(form)
    setErrores(fallas)
    setError('')
    if (!sinErrores(fallas)) return
    setOcupado(true)
    try {
      const respuesta = await auth.ingresar(esPersonal ? 'personal' : 'cliente', {
        email: form.email.trim(),
        clave: form.clave,
      })
      entrar(respuesta)
      const inicio = rutaInicio(respuesta.usuario)
      const destino = state?.desde?.startsWith(inicio) ? state.desde : inicio
      navegar(destino, { replace: true })
    } catch (err) {
      setError(mensajeError(err))
    } finally {
      setOcupado(false)
    }
  }

  return (
    <main className="contenedor py-16">
      <div className="form-card">
        <div className={esPersonal ? 'form-insignia personal' : 'form-insignia'}>
          {esPersonal ? 'PERSONAL AUTORIZADO' : 'PORTAL DE CLIENTES'}
        </div>
        <h1 className="titulo-seccion mt-3">{esPersonal ? 'Acceso para técnicos y administradores' : 'Bienvenido de nuevo'}</h1>
        <p className="texto-apoyo mb-6">
          {esPersonal ? 'Atiende órdenes o administra la operación.' : 'Consulta tus atenciones y solicita soporte.'}
        </p>
        <div className="flex rounded-xl p-1 bg-slate-100 mb-6">
          <Link className={`selector ${!esPersonal ? 'seleccionado' : ''}`} to="/acceso/cliente">
            Cliente
          </Link>
          <Link className={`selector ${esPersonal ? 'seleccionado' : ''}`} to="/acceso/personal">
            Personal
          </Link>
        </div>
        {aviso && <p className="aviso mb-4">{aviso}</p>}
        <form onSubmit={enviar} noValidate className="space-y-4">
          <Campo id="email" etiqueta="Correo electrónico" error={errores.email}>
            <input
              className="campo"
              id="email"
              name="email"
              type="email"
              autoComplete="username"
              value={form.email}
              onChange={cambiar}
              aria-invalid={Boolean(errores.email)}
            />
          </Campo>
          <Campo id="clave" etiqueta="Contraseña" error={errores.clave}>
            <input
              className="campo"
              id="clave"
              name="clave"
              type="password"
              autoComplete="current-password"
              value={form.clave}
              onChange={cambiar}
              aria-invalid={Boolean(errores.clave)}
            />
          </Campo>
          <Mensajes error={error} />
          <button className="btn btn-oscuro w-full justify-center" type="submit" disabled={ocupado}>
            {ocupado ? 'Validando…' : 'Ingresar'}
          </button>
        </form>
        {esPersonal ? (
          <p className="text-xs text-slate-500 mt-6">
            Las cuentas de técnicos y administradores las crea un administrador. No hay registro público de personal.
          </p>
        ) : (
          <p className="text-sm text-slate-600 mt-6 text-center">
            ¿Aún no tienes cuenta?{' '}
            <Link className="link" to="/registro">
              Regístrate aquí
            </Link>
          </p>
        )}
      </div>
    </main>
  )
}
