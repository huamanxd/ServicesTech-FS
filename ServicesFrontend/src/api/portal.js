import { http } from './http'

const datos = (promesa) => promesa.then((r) => r.data)

/** Endpoints públicos. */
export const publico = {
  servicios: (config) => datos(http.get('/servicios', config)),
  prioridades: (config) => datos(http.get('/prioridades', config)),
}

export const auth = {
  registro: (cuerpo) => datos(http.post('/auth/registro', cuerpo)),
  ingresar: (portal, cuerpo) => datos(http.post(`/auth/${portal}`, cuerpo)),
  perfil: (config) => datos(http.get('/auth/perfil', config)),
  salir: () => datos(http.post('/auth/salir')),
}

export const cliente = {
  dashboard: () => datos(http.get('/cliente/dashboard')),
  solicitudes: () => datos(http.get('/cliente/solicitudes')),
  registrar: (cuerpo) => datos(http.post('/cliente/solicitudes', cuerpo)),
  cancelar: (id) => datos(http.post(`/cliente/solicitudes/${id}/cancelar`)),
  historial: (ordenId) => datos(http.get(`/cliente/ordenes/${ordenId}/historial`)),
  calificar: (ordenId, cuerpo) => datos(http.post(`/cliente/ordenes/${ordenId}/satisfaccion`, cuerpo)),
}

export const tecnico = {
  dashboard: () => datos(http.get('/tecnico/dashboard')),
  disponibles: () => datos(http.get('/tecnico/ordenes/disponibles')),
  ordenes: () => datos(http.get('/tecnico/ordenes')),
  tomar: (id) => datos(http.post(`/tecnico/ordenes/${id}/tomar`)),
  iniciar: (id, observacion) => datos(http.post(`/tecnico/ordenes/${id}/iniciar`, { observacion })),
  solucion: (id, descripcion) => datos(http.post(`/tecnico/ordenes/${id}/solucion`, { descripcion })),
  historial: (id) => datos(http.get(`/tecnico/ordenes/${id}/historial`)),
}

export const admin = {
  dashboard: () => datos(http.get('/admin/dashboard')),
  solicitudes: (estado) => datos(http.get('/admin/solicitudes', { params: { estado: estado || undefined } })),
  revisar: (id, estado) => datos(http.post(`/admin/solicitudes/${id}/revision`, { estado })),
  generarOrden: (id, cuerpo) => datos(http.post(`/admin/solicitudes/${id}/orden`, cuerpo)),
  ordenes: (estado) => datos(http.get('/admin/ordenes', { params: { estado: estado || undefined } })),
  asignar: (id, tecnicoId, observacion) => datos(http.post(`/admin/ordenes/${id}/asignar`, { tecnicoId, observacion })),
  cerrar: (id, observacion) => datos(http.post(`/admin/ordenes/${id}/cerrar`, { observacion })),
  cancelar: (id, observacion) => datos(http.post(`/admin/ordenes/${id}/cancelar`, { observacion })),
  historial: (id) => datos(http.get(`/admin/ordenes/${id}/historial`)),
  servicios: () => datos(http.get('/admin/servicios')),
  crearServicio: (cuerpo) => datos(http.post('/admin/servicios', cuerpo)),
  editarServicio: (id, cuerpo) => datos(http.put(`/admin/servicios/${id}`, cuerpo)),
  desactivarServicio: (id) => datos(http.delete(`/admin/servicios/${id}`)),
  usuarios: (rol) => datos(http.get('/admin/usuarios', { params: { rol: rol || undefined } })),
  crearUsuario: (cuerpo) => datos(http.post('/admin/usuarios', cuerpo)),
  cambiarActivo: (id, activo) => datos(http.patch(`/admin/usuarios/${id}/estado`, { activo })),
}
