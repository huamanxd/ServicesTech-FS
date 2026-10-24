import { Outlet } from 'react-router-dom'
import Cabecera from '../componentes/Cabecera'
import Piedepagina from '../componentes/Piedepagina'

/** Estructura general del sitio: cabecera, contenido de la ruta y pie. */
export default function Home() {
  return (
    <div className="min-h-screen flex flex-col">
      <Cabecera />
      <div className="flex-1 flex flex-col">
        <Outlet />
      </div>
      <Piedepagina />
    </div>
  )
}
