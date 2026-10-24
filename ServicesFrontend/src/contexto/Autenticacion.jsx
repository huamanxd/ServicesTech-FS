import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react'
import { auth } from '../api/portal'
import { EVENTO_SESION_EXPIRADA, guardarToken, leerToken } from '../api/http'

const Contexto = createContext(null)

export function ProveedorAutenticacion({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [cargando, setCargando] = useState(() => Boolean(leerToken()))
  const [aviso, setAviso] = useState('')

  // Al abrir la app con un token guardado, se valida contra el backend.
  useEffect(() => {
    if (!leerToken()) return undefined
    const controlador = new AbortController()
    auth
      .perfil({ signal: controlador.signal })
      .then(setUsuario)
      .catch((error) => {
        if (error.code !== 'ERR_CANCELED') {
          guardarToken(null)
          setUsuario(null)
        }
      })
      .finally(() => {
        if (!controlador.signal.aborted) setCargando(false)
      })
    return () => controlador.abort()
  }, [])

  // Cualquier 401 con token (vencido, revocado o cuenta desactivada) cierra la sesión local.
  useEffect(() => {
    // El evento solo se emite si la petición llevaba token, es decir, había una sesión abierta.
    const alExpirar = () => {
      setAviso('Tu sesión venció o fue cerrada. Vuelve a iniciar sesión.')
      setUsuario(null)
    }
    window.addEventListener(EVENTO_SESION_EXPIRADA, alExpirar)
    return () => window.removeEventListener(EVENTO_SESION_EXPIRADA, alExpirar)
  }, [])

  const entrar = useCallback((respuesta) => {
    guardarToken(respuesta.token)
    setAviso('')
    setUsuario(respuesta.usuario)
  }, [])

  const salir = useCallback(async () => {
    try {
      if (leerToken()) await auth.salir()
    } catch {
      /* el token ya no era válido */
    }
    guardarToken(null)
    setUsuario(null)
  }, [])

  const valor = useMemo(
    () => ({ usuario, cargando, aviso, entrar, salir, limpiarAviso: () => setAviso('') }),
    [usuario, cargando, aviso, entrar, salir],
  )
  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAutenticacion() {
  const contexto = useContext(Contexto)
  if (!contexto) throw new Error('useAutenticacion debe usarse dentro de ProveedorAutenticacion')
  return contexto
}
