import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAutenticacion } from '../contexto/Autenticacion.jsx'

const estilo = ({isActive}) => `hover:text-sky-600 ${isActive?'font-bold text-sky-700':''}`
export default function Cabecera() {
  const {usuario, salir}=useAutenticacion()
  const navegar=useNavigate()
  async function cerrar(){ await salir(); navegar('/') }
  return <header className="w-full bg-white text-black border-b border-gray-200 shadow-sm">
    <div className="mx-auto max-w-7xl px-3 py-2 flex flex-wrap items-center justify-between gap-4">
      <Link to="/" aria-label="Ir al inicio"><img src="/assets/logoST.svg" className="h-16 w-auto" alt="ServicesTech" /></Link>
      <nav className="flex flex-wrap items-center gap-3 text-sm" aria-label="Portales de acceso">
        {usuario ? <><Link to={usuario.rol==='ADMIN'?'/admin':'/cliente'} className="bg-sky-50 text-sky-900 font-bold px-3 py-2 rounded-xl border border-sky-200">{usuario.rol==='ADMIN'?'Panel de administración':'Mi cuenta'}</Link>
          <button type="button" onClick={cerrar} className="bg-black text-white px-3 py-2 rounded-xl font-bold">Cerrar sesión</button></>
          : <><details className="relative group"><summary className="list-none cursor-pointer bg-sky-50 text-orange-800 font-bold px-3 py-2 rounded-xl border border-orange-200">Portal Cliente ▾</summary>
            <div className="absolute z-50 right-0 top-full mt-2 w-48 bg-white border rounded-xl shadow-lg overflow-hidden">
              <Link className="block px-4 py-3 hover:bg-sky-50" to="/login/cliente">Iniciar sesión</Link>
              <Link className="block px-4 py-3 hover:bg-orange-50" to="/signup">Crear una cuenta</Link>
            </div></details>
            <Link to="/login/personal" className="bg-black text-white px-3 py-2 font-bold rounded-xl border">Acceso Personal</Link></>}
      </nav>
      <nav className="flex flex-wrap gap-x-5 gap-y-2 items-center text-sm" aria-label="Navegación principal">
        <NavLink to="/" end className={estilo}>Inicio</NavLink>
        <NavLink to="/servicios" className={estilo}>Servicios</NavLink>
        <NavLink to="/catalogo" className={estilo}>Catálogo</NavLink>
        <NavLink to="/nosotros" className={estilo}>Nosotros</NavLink>
        <NavLink to="/casos" className={estilo}>Casos</NavLink>
        <NavLink to="/blog" className={estilo}>Blog</NavLink>
        <NavLink to="/contacto" className={estilo}>Contacto</NavLink>
        <a className="text-green-900 px-3 py-2 font-bold rounded-xl border-2 border-green-500" href="https://api.whatsapp.com/send/?phone=51928536863&amp;text=Hola+ServicesTech" target="_blank" rel="noopener noreferrer">WhatsApp</a>
      </nav>
    </div>
  </header>
}
