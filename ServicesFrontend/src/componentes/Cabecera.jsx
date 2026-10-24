import { useEffect, useRef, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAutenticacion } from '../contexto/Autenticacion'
import { panelDe } from '../util/roles'

const estiloEnlace = ({ isActive }) => `hover:text-sky-600 ${isActive ? 'font-bold text-sky-700' : ''}`
const WHATSAPP = 'https://api.whatsapp.com/send/?phone=51928536863&text=Hola+ServicesTech'

export default function Cabecera() {
  const { usuario, salir } = useAutenticacion()
  const navegar = useNavigate()
  const panel = panelDe(usuario)

  async function cerrarSesion() {
    await salir()
    navegar('/')
  }

  return (
    <header className="w-full bg-white text-black border-b border-gray-200 shadow-sm">
      <div className="mx-auto max-w-7xl px-3 py-2 flex flex-wrap items-center justify-between gap-4">
        <Link to="/" aria-label="Ir al inicio">
          <img src="/assets/logoST.svg" className="h-16 w-auto" alt="ServicesTech" />
        </Link>

        <nav className="flex flex-wrap items-center gap-3 text-sm" aria-label="Accesos">
          {usuario ? (
            <>
              <span className="texto-suave hidden sm:inline">Hola, {usuario.nombres}</span>
              <Link to={panel.ruta} className="bg-sky-50 text-sky-900 font-bold px-3 py-2 rounded-xl border border-sky-200">
                {panel.etiqueta}
              </Link>
              <button type="button" onClick={cerrarSesion} className="bg-black text-white px-3 py-2 rounded-xl font-bold">
                Cerrar sesión
              </button>
            </>
          ) : (
            <>
              <MenuPortalCliente />
              <Link to="/acceso/personal" className="bg-black text-white px-3 py-2 font-bold rounded-xl border">
                Acceso Personal
              </Link>
            </>
          )}
        </nav>

        <nav className="flex flex-wrap gap-x-5 gap-y-2 items-center text-sm" aria-label="Navegación principal">
          <NavLink to="/" end className={estiloEnlace}>
            Inicio
          </NavLink>
          <NavLink to="/catalogo" className={estiloEnlace}>
            Servicios
          </NavLink>
          <NavLink to="/nosotros" className={estiloEnlace}>
            Nosotros
          </NavLink>
          <NavLink to="/casos" className={estiloEnlace}>
            Casos
          </NavLink>
          <NavLink to="/blog" className={estiloEnlace}>
            Blog
          </NavLink>
          <NavLink to="/contacto" className={estiloEnlace}>
            Contacto
          </NavLink>
          <a
            className="text-green-900 px-3 py-2 font-bold rounded-xl border-2 border-green-500"
            href={WHATSAPP}
            target="_blank"
            rel="noopener noreferrer"
          >
            WhatsApp
          </a>
        </nav>
      </div>
    </header>
  )
}

/**
 * Menú desplegable «Portal Cliente». Se cierra al hacer clic fuera, al pulsar Escape
 * o al elegir una opción. Al iniciar sesión el componente se desmonta y su estado se reinicia.
 */
function MenuPortalCliente() {
  const [abierto, setAbierto] = useState(false)
  const contenedor = useRef(null)
  const boton = useRef(null)

  useEffect(() => {
    if (!abierto) return undefined
    const alPulsarFuera = (e) => {
      if (!contenedor.current?.contains(e.target)) setAbierto(false)
    }
    const alTeclear = (e) => {
      if (e.key === 'Escape') {
        setAbierto(false)
        boton.current?.focus()
      }
    }
    document.addEventListener('pointerdown', alPulsarFuera)
    document.addEventListener('keydown', alTeclear)
    return () => {
      document.removeEventListener('pointerdown', alPulsarFuera)
      document.removeEventListener('keydown', alTeclear)
    }
  }, [abierto])

  const cerrar = () => setAbierto(false)

  return (
    <div className="relative" ref={contenedor}>
      <button
        ref={boton}
        type="button"
        aria-expanded={abierto}
        aria-controls="menu-portal-cliente"
        onClick={() => setAbierto((v) => !v)}
        className="bg-sky-50 text-orange-800 font-bold px-3 py-2 rounded-xl border border-orange-200"
      >
        Portal Cliente {abierto ? '▴' : '▾'}
      </button>
      {abierto && (
        <div
          id="menu-portal-cliente"
          className="absolute z-50 right-0 top-full mt-2 w-48 bg-white border rounded-xl shadow-lg overflow-hidden"
        >
          <Link className="block px-4 py-3 hover:bg-sky-50" to="/acceso/cliente" onClick={cerrar}>
            Iniciar sesión
          </Link>
          <Link className="block px-4 py-3 hover:bg-orange-50" to="/registro" onClick={cerrar}>
            Crear una cuenta
          </Link>
        </div>
      )}
    </div>
  )
}
