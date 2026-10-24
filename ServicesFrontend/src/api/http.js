import axios from 'axios'

/** Clave de sessionStorage donde vive el token de la pestaña actual. */
export const CLAVE_TOKEN = 'st_token'
/** Evento global que se emite cuando el backend rechaza el token (vencido o revocado). */
export const EVENTO_SESION_EXPIRADA = 'st:sesion-expirada'

// Siempre mismo origen: Vite (dev), Nginx (Docker) y Vercel (rewrite) reenvían /api al backend.
const baseURL = import.meta.env.VITE_API_URL?.trim() || '/api'

export const http = axios.create({
  baseURL,
  // Render (plan gratuito) puede tardar ~1 minuto en despertar el backend.
  timeout: 70000,
  headers: { 'Content-Type': 'application/json' },
})

export function leerToken() {
  try {
    return sessionStorage.getItem(CLAVE_TOKEN)
  } catch {
    return null
  }
}

export function guardarToken(token) {
  try {
    if (token) sessionStorage.setItem(CLAVE_TOKEN, token)
    else sessionStorage.removeItem(CLAVE_TOKEN)
  } catch {
    /* almacenamiento no disponible: la sesión dura lo que la pestaña */
  }
}

http.interceptors.request.use((config) => {
  const token = leerToken()
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

http.interceptors.response.use(
  (respuesta) => respuesta,
  (error) => {
    const enviabaToken = Boolean(error.config?.headers?.Authorization)
    if (error.response?.status === 401 && enviabaToken) {
      guardarToken(null)
      window.dispatchEvent(new Event(EVENTO_SESION_EXPIRADA))
    }
    return Promise.reject(error)
  },
)

/** Convierte cualquier error de Axios en un mensaje legible para el usuario. */
export function mensajeError(err) {
  if (err?.code === 'ERR_CANCELED') return 'La solicitud fue cancelada.'
  if (err?.code === 'ECONNABORTED') {
    return 'El servidor está tardando en responder (puede estar iniciándose). Inténtalo de nuevo en unos segundos.'
  }
  if (!err?.response) return 'No se pudo conectar con el servidor. Revisa tu conexión e inténtalo nuevamente.'

  const { status, data } = err.response
  const { message, fields } = data && typeof data === 'object' ? data : {}
  const detalles = Object.values(fields || {}).filter(Boolean).join(' · ')
  if (detalles) return detalles
  if (message) return message
  if (status === 401) return 'Tu sesión venció. Vuelve a iniciar sesión.'
  if (status === 403) return 'No tienes permisos para esta operación.'
  if (status === 404) return 'No se encontró el recurso solicitado.'
  if (status >= 500) return 'El servidor tuvo un problema. Inténtalo nuevamente.'
  return `Error inesperado (HTTP ${status}).`
}
