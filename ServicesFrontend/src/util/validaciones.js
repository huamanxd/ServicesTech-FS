export const EMAIL = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
export const TELEFONO = /^9\d{8}$/
export const SOLES = new Intl.NumberFormat('es-PE', { style: 'currency', currency: 'PEN' })
export function validarRegistro(p) {
  const errores = {}
  if (!p.nombres?.trim() || p.nombres.trim().length < 2 || p.nombres.trim().length > 90) errores.nombres = 'Ingresa tus nombres (mínimo 2 caracteres).'
  if (!p.apellidos?.trim() || p.apellidos.trim().length < 2 || p.apellidos.trim().length > 90) errores.apellidos = 'Ingresa tus apellidos (mínimo 2 caracteres).'
  if (!EMAIL.test(p.email?.trim() || '') || (p.email || '').length > 180) errores.email = 'Correo electrónico inválido.'
  if (p.telefono && !TELEFONO.test(p.telefono)) errores.telefono = 'Debe comenzar con 9 y tener 9 dígitos.'
  if ((p.clave || '').length < 10 || (p.clave || '').length > 72) errores.clave = 'La contraseña requiere al menos 10 caracteres.'
  if (p.confirmar !== p.clave) errores.confirmar = 'Las contraseñas no coinciden.'
  return errores
}
export function validarAcceso(p) {
  const errores = {}
  if (!EMAIL.test(p.email?.trim() || '')) errores.email = 'Introduce un correo válido.'
  if (!p.clave) errores.clave = 'Introduce tu contraseña.'
  return errores
}
export function validarSolicitud(p) {
  const errores = {}
  if (!p.servicioId) errores.servicioId = 'Selecciona un servicio.'
  if ((p.detalle?.trim() || '').length < 10 || (p.detalle?.trim() || '').length > 700) errores.detalle = 'Describe tu problema en al menos 10 caracteres.'
  return errores
}
