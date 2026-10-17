import { Navigate, useLocation } from 'react-router-dom'
import { useAutenticacion } from '../contexto/Autenticacion'
export default function Restringida({ rol, children }) {
  const { usuario, cargando } = useAutenticacion()
  const origen = useLocation()
  if (cargando) return <main className="contenedor py-20" role="status">Verificando tu sesión…</main>
  if (!usuario) return <Navigate replace to={rol === 'ADMIN' ? '/acceso/personal' : '/acceso/cliente'} state={{ desde: origen.pathname }} />
  if (usuario.rol !== rol) return <Navigate replace to={usuario.rol === 'ADMIN' ? '/admin' : '/cliente'} />
  return children
}
