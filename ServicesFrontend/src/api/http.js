import axios from 'axios'

// Con Vite se utiliza /api (proxy hacia Spring Boot), sin CORS en el navegador.
// Para un despliegue externo se puede establecer VITE_API_URL explicitamente.
const baseURL = import.meta.env.VITE_API_URL?.trim() || '/api'

export const http = axios.create({
  baseURL,
  timeout: 12000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('st_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export function mensajeError(err) {
  if (err.code === 'ERR_CANCELED') return 'La solicitud fue cancelada.'
  if (err.code === 'ECONNABORTED') return 'La solicitud tardó demasiado. Comprueba la conexión con el backend.'
  // Un fallo sin respuesta puede ser proxy, red o CORS; no significa que Spring Boot este apagado.
  if (!err.response) return 'No se recibió respuesta de la API. Revisa el proxy de Vite, la dirección del backend y la consola del navegador (CORS/red).'

  const { status, data } = err.response
  const { message, fields } = data && typeof data === 'object' ? data : {}
  const detalles = Object.values(fields || {}).filter(Boolean).join(' · ')
  if (detalles) return detalles
  if (message) return message

  if (status === 401) return 'Sesión no válida o vencida. Vuelve a iniciar sesión.'
  if (status === 403) return 'No tienes permisos para esta operación.'
  if (status === 404) return 'La ruta solicitada no existe en la API.'
  if (status >= 500) return 'El backend devolvió un error interno. Revisa los registros de Spring Boot.'
  return `La API devolvió el error HTTP ${status}.`
}
