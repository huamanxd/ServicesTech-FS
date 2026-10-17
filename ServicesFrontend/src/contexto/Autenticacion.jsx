import { createContext, useContext, useEffect, useState } from 'react'
import { http } from '../api/http'
const Contexto = createContext(null)
export function ProveedorAutenticacion({ children }) {
  const [usuario, setUsuario] = useState(null)
  const [cargando, setCargando] = useState(true)
  useEffect(() => {
    const controlador = new AbortController()
    async function verificar() {
      if (!sessionStorage.getItem('st_token')) { setCargando(false); return }
      try { const { data } = await http.get('/auth/perfil', { signal: controlador.signal }); setUsuario(data) }
      catch (error) {
        if (error.code !== 'ERR_CANCELED') { sessionStorage.removeItem('st_token'); setUsuario(null) }
      } finally { if (!controlador.signal.aborted) setCargando(false) }
    }
    verificar()
    return () => controlador.abort()
  }, [])
  function entrar(respuesta) { sessionStorage.setItem('st_token', respuesta.token); setUsuario(respuesta.usuario) }
  async function salir() {
    try { if (sessionStorage.getItem('st_token')) await http.post('/auth/salir') } catch { /* token ya vencido */ }
    sessionStorage.removeItem('st_token'); setUsuario(null)
  }
  return <Contexto.Provider value={{ usuario, cargando, entrar, salir }}>{children}</Contexto.Provider>
}
export function useAutenticacion() {
  const contexto = useContext(Contexto)
  if (!contexto) throw new Error('Falta ProveedorAutenticacion')
  return contexto
}
