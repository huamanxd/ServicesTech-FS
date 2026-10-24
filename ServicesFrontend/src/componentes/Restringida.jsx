import { Navigate, useLocation } from 'react-router-dom'
import { useAutenticacion } from '../contexto/Autenticacion'
import { ROLES, rutaInicio, tieneRol } from '../util/roles'

/** Protege una ruta por rol. La autorización real la vuelve a hacer Spring Security. */
export default function Restringida({ rol, children }) {
  const { usuario, cargando } = useAutenticacion()
  const ubicacion = useLocation()

  if (cargando) {
    return (
      <main className="contenedor py-20" role="status">
        Verificando tu sesión…
      </main>
    )
  }
  if (!usuario) {
    const portal = rol === ROLES.CLIENTE ? 'cliente' : 'personal'
    return <Navigate replace to={`/acceso/${portal}`} state={{ desde: ubicacion.pathname + ubicacion.search }} />
  }
  if (!tieneRol(usuario, rol)) return <Navigate replace to={rutaInicio(usuario)} />
  return children
}
