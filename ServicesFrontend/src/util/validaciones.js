export const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
export const TELEFONO = /^9\d{8}$/
export const DNI = /^\d{8}$/
export const CODIGO_SERVICIO = /^[A-Z0-9-]{3,45}$/

const largo = (texto) => (texto ?? '').trim().length

/** Mismas reglas que Peticiones.Registro en el backend. */
export function validarRegistro(p) {
  const e = {}
  if (largo(p.nombres) < 2 || largo(p.nombres) > 90) e.nombres = 'Ingresa los nombres (mínimo 2 caracteres).'
  if (largo(p.apellidos) < 2 || largo(p.apellidos) > 90) e.apellidos = 'Ingresa los apellidos (mínimo 2 caracteres).'
  if (!DNI.test(p.dni ?? '')) e.dni = 'El DNI debe tener 8 dígitos.'
  if (!EMAIL.test((p.email ?? '').trim()) || (p.email ?? '').length > 180) e.email = 'Correo electrónico inválido.'
  if (!TELEFONO.test(p.telefono ?? '')) e.telefono = 'El celular debe empezar con 9 y tener 9 dígitos.'
  if ((p.clave ?? '').length < 10 || (p.clave ?? '').length > 72) e.clave = 'La contraseña debe tener entre 10 y 72 caracteres.'
  if (p.confirmar !== p.clave) e.confirmar = 'Las contraseñas no coinciden.'
  return e
}

export function validarPersonal(p) {
  const e = validarRegistro(p)
  if (!['TECNICO', 'ADMINISTRADOR'].includes(p.rol)) e.rol = 'Selecciona el rol.'
  return e
}

export function validarAcceso(p) {
  const e = {}
  if (!EMAIL.test((p.email ?? '').trim())) e.email = 'Introduce un correo válido.'
  if (!p.clave) e.clave = 'Introduce tu contraseña.'
  return e
}

export function validarSolicitud(p) {
  const e = {}
  if (!p.servicioId) e.servicioId = 'Selecciona un servicio.'
  if (!p.prioridadId) e.prioridadId = 'Selecciona la prioridad.'
  if (largo(p.asunto) < 5 || largo(p.asunto) > 150) e.asunto = 'Resume el problema en 5 a 150 caracteres.'
  if (largo(p.descripcion) < 10 || largo(p.descripcion) > 700) e.descripcion = 'Describe el problema en al menos 10 caracteres.'
  if (largo(p.direccion) > 220) e.direccion = 'Máximo 220 caracteres.'
  return e
}

export function validarServicio(p) {
  const e = {}
  if (!CODIGO_SERVICIO.test(p.codigo ?? '')) e.codigo = 'Usa de 3 a 45 caracteres: A-Z, 0-9 o guion.'
  if (!largo(p.nombre) || largo(p.nombre) > 120) e.nombre = 'Ingresa el nombre (máx. 120).'
  if (!largo(p.descripcion) || largo(p.descripcion) > 600) e.descripcion = 'Ingresa la descripción (máx. 600).'
  const precio = Number(p.precioReferencial)
  if (p.precioReferencial === '' || !Number.isFinite(precio) || precio < 0) e.precioReferencial = 'Precio inválido.'
  const minutos = Number(p.duracionMinutos)
  if (!Number.isInteger(minutos) || minutos < 15 || minutos > 10080) e.duracionMinutos = 'Entre 15 y 10080 minutos.'
  return e
}

export const sinErrores = (errores) => Object.keys(errores).length === 0
