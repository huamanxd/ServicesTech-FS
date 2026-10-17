import test from 'node:test'
import assert from 'node:assert/strict'
import { validarRegistro, validarAcceso, validarSolicitud, SOLES } from '../src/util/validaciones.js'
test('registro válido con teléfono peruano',()=>assert.deepEqual(validarRegistro({nombres:'Ana',apellidos:'Pérez',email:'ana@example.com',telefono:'987654321',clave:'ejemplo1234',confirmar:'ejemplo1234'}),{}))
test('registro rechaza claves diferentes y números inválidos',()=>{const e=validarRegistro({nombres:'A',apellidos:'B',email:'mal',telefono:'123',clave:'123',confirmar:'1234'});assert.ok(e.clave&&e.confirmar&&e.telefono&&e.email)})
test('login necesita correo y contraseña',()=>assert.ok(validarAcceso({email:'falso',clave:''}).email))
test('solicitud necesita servicio y descripción clara',()=>assert.ok(validarSolicitud({servicioId:'',detalle:'breve'}).detalle))
test('referencia monetaria usa PEN',()=>assert.match(SOLES.format(95),/95/))
