import { describe, expect, it } from 'vitest'
import { mensajeError } from '../src/api/http'
import { estadoAtencion, etiqueta, horas, porcentaje, SOLES } from '../src/util/formato'
import { panelDe, rutaInicio, tieneRol } from '../src/util/roles'

describe('roles', () => {
  it('lleva a cada rol a su panel', () => {
    expect(rutaInicio({ roles: ['CLIENTE'] })).toBe('/cliente')
    expect(rutaInicio({ roles: ['TECNICO'] })).toBe('/tecnico')
    expect(rutaInicio({ roles: ['ADMINISTRADOR'] })).toBe('/admin')
    expect(panelDe({ roles: ['TECNICO', 'ADMINISTRADOR'] }).ruta).toBe('/admin')
    expect(tieneRol(null, 'CLIENTE')).toBe(false)
  })
})

describe('formato', () => {
  it('muestra etiquetas, montos y duraciones legibles', () => {
    expect(etiqueta('EN_ATENCION')).toBe('En atención')
    expect(SOLES.format(95)).toMatch(/95/)
    expect(horas(5)).toBe('5.0 h')
    expect(horas(72)).toBe('3.0 días')
    expect(horas(null)).toBe('—')
    expect(porcentaje(50)).toBe('50.0 %')
  })

  it('el estado de atención es el de la orden si existe', () => {
    expect(estadoAtencion({ estado: 'APROBADA', orden: { estado: 'EN_ATENCION' } })).toBe('EN_ATENCION')
    expect(estadoAtencion({ estado: 'PENDIENTE', orden: null })).toBe('PENDIENTE')
  })
})

describe('mensajeError', () => {
  it('prioriza los errores de campo del backend', () => {
    const err = { response: { status: 400, data: { message: 'Revisa', fields: { dni: 'El DNI debe tener 8 dígitos' } } } }
    expect(mensajeError(err)).toBe('El DNI debe tener 8 dígitos')
  })

  it('explica el tiempo de espera del arranque en frío', () => {
    expect(mensajeError({ code: 'ECONNABORTED' })).toMatch(/iniciándose/)
  })

  it('usa el mensaje del backend o uno genérico por estado', () => {
    expect(mensajeError({ response: { status: 409, data: { message: 'Ya existe' } } })).toBe('Ya existe')
    expect(mensajeError({ response: { status: 403, data: null } })).toMatch(/permisos/)
    expect(mensajeError({})).toMatch(/conectar/)
  })
})
