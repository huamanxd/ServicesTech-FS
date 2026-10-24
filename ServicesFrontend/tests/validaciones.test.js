import { describe, expect, it } from 'vitest'
import { sinErrores, validarAcceso, validarPersonal, validarRegistro, validarServicio, validarSolicitud } from '../src/util/validaciones'

const persona = {
  nombres: 'Ana',
  apellidos: 'Pérez',
  dni: '12345678',
  email: 'ana@example.com',
  telefono: '987654321',
  clave: 'ejemplo1234',
  confirmar: 'ejemplo1234',
}

describe('validaciones (mismas reglas que el backend)', () => {
  it('acepta un registro completo', () => {
    expect(validarRegistro(persona)).toEqual({})
  })

  it('exige DNI de 8 dígitos, celular peruano y claves iguales', () => {
    const e = validarRegistro({ ...persona, dni: '123', telefono: '123', confirmar: 'otra' })
    expect(Object.keys(e).sort()).toEqual(['confirmar', 'dni', 'telefono'])
  })

  it('el alta de personal requiere un rol válido', () => {
    expect(validarPersonal({ ...persona, rol: 'CLIENTE' }).rol).toBeTruthy()
    expect(sinErrores(validarPersonal({ ...persona, rol: 'TECNICO' }))).toBe(true)
  })

  it('el acceso necesita correo y contraseña', () => {
    expect(validarAcceso({ email: 'falso', clave: '' })).toHaveProperty('email')
    expect(validarAcceso({ email: 'falso', clave: '' })).toHaveProperty('clave')
  })

  it('la solicitud necesita servicio, prioridad, asunto y descripción', () => {
    const e = validarSolicitud({ servicioId: '', prioridadId: '', asunto: 'pc', descripcion: 'corta' })
    expect(Object.keys(e).sort()).toEqual(['asunto', 'descripcion', 'prioridadId', 'servicioId'])
  })

  it('valida código, precio y duración del servicio', () => {
    const e = validarServicio({ codigo: 'abc', nombre: 'X', descripcion: 'Y', precioReferencial: '-1', duracionMinutos: 5 })
    expect(Object.keys(e).sort()).toEqual(['codigo', 'duracionMinutos', 'precioReferencial'])
  })
})
