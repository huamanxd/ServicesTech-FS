export const ROLES = Object.freeze({ CLIENTE: 'CLIENTE', TECNICO: 'TECNICO', ADMINISTRADOR: 'ADMINISTRADOR' })

const PANELES = [
  [ROLES.ADMINISTRADOR, '/admin', 'Panel de administración'],
  [ROLES.TECNICO, '/tecnico', 'Panel del técnico'],
  [ROLES.CLIENTE, '/cliente', 'Mi cuenta'],
]

export function tieneRol(usuario, rol) {
  return Boolean(usuario?.roles?.includes(rol))
}

/** Panel principal del usuario (si tiene varios roles, gana el de mayor responsabilidad). */
export function panelDe(usuario) {
  const encontrado = PANELES.find(([rol]) => tieneRol(usuario, rol))
  return encontrado ? { ruta: encontrado[1], etiqueta: encontrado[2] } : { ruta: '/', etiqueta: 'Inicio' }
}

export const rutaInicio = (usuario) => panelDe(usuario).ruta
