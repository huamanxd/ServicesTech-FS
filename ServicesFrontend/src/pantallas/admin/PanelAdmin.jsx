import { useState } from 'react'
import { Pestanas } from '../../componentes/Ui'
import { useAutenticacion } from '../../contexto/Autenticacion'
import CatalogoAdmin from './CatalogoAdmin'
import DashboardAdmin from './DashboardAdmin'
import OrdenesAdmin from './OrdenesAdmin'
import SolicitudesAdmin from './SolicitudesAdmin'
import UsuariosAdmin from './UsuariosAdmin'

const PESTANAS = [
  ['dashboard', 'Dashboard'],
  ['solicitudes', 'Solicitudes'],
  ['ordenes', 'Órdenes'],
  ['catalogo', 'Catálogo'],
  ['usuarios', 'Usuarios'],
]

export default function PanelAdmin() {
  const { usuario } = useAutenticacion()
  const [pestana, setPestana] = useState('dashboard')

  return (
    <main className="contenedor py-12">
      <span className="etiqueta">ÁREA DE ADMINISTRACIÓN</span>
      <h1 className="titulo-pagina">Panel de administración</h1>
      <p className="texto-apoyo mb-8">
        Sesión: {usuario.nombres} {usuario.apellidos}. Revisa solicitudes, genera órdenes, asigna técnicos y supervisa
        los indicadores.
      </p>
      <Pestanas opciones={PESTANAS} activa={pestana} onCambio={setPestana} etiquetaGrupo="Secciones del administrador" />
      {pestana === 'dashboard' && <DashboardAdmin />}
      {pestana === 'solicitudes' && <SolicitudesAdmin />}
      {pestana === 'ordenes' && <OrdenesAdmin />}
      {pestana === 'catalogo' && <CatalogoAdmin />}
      {pestana === 'usuarios' && <UsuariosAdmin usuarioActual={usuario} />}
    </main>
  )
}
