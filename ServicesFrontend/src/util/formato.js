export const SOLES = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })

const FECHA = new Intl.DateTimeFormat('es-PE', { dateStyle: 'medium', timeStyle: 'short', timeZone: 'America/Lima' })

export function fecha(iso) {
  return iso ? FECHA.format(new Date(iso)) : '—'
}

export function horas(valor) {
  if (valor === null || valor === undefined) return '—'
  if (valor >= 48) return `${(valor / 24).toFixed(1)} días`
  return `${valor.toFixed(1)} h`
}

export function porcentaje(valor) {
  return valor === null || valor === undefined ? '—' : `${valor.toFixed(1)} %`
}

export function promedio(valor) {
  return valor === null || valor === undefined ? '—' : `${valor.toFixed(2)} / 5`
}

const ETIQUETAS = {
  PENDIENTE: 'Pendiente',
  EN_REVISION: 'En revisión',
  APROBADA: 'Aprobada',
  RECHAZADA: 'Rechazada',
  CANCELADA: 'Cancelada',
  ASIGNADA: 'Asignada',
  EN_ATENCION: 'En atención',
  RESUELTA: 'Resuelta',
  CERRADA: 'Cerrada',
  BAJA: 'Baja',
  MEDIA: 'Media',
  ALTA: 'Alta',
  CRITICA: 'Crítica',
  CLIENTE: 'Cliente',
  TECNICO: 'Técnico',
  ADMINISTRADOR: 'Administrador',
}

export function etiqueta(codigo) {
  return ETIQUETAS[codigo] ?? codigo
}

/** Estado que ve el cliente: el de la orden si ya existe; si no, el de la solicitud. */
export function estadoAtencion(solicitud) {
  return solicitud.orden?.estado ?? solicitud.estado
}

/** Convierte el valor de un input datetime-local (hora local) a ISO-8601 UTC. */
export function localAIso(valor) {
  return valor ? new Date(valor).toISOString() : null
}
