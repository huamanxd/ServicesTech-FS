// Se conserva Home.jsx como contenedor principal del sitio original.
import { Outlet } from 'react-router-dom'
import Cabecera from '../componentes/Cabecera.jsx'
import Piedepagina from '../componentes/Piedepagina.jsx'

export default function Home() {
  return <div className="min-h-screen flex flex-col">
    <Cabecera />
    <div className="flex-1 flex flex-col"><Outlet /></div>
    <Piedepagina />
  </div>
}
