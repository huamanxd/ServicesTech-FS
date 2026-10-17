import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'

const read = (path) => readFileSync(new URL(path, import.meta.url), 'utf8')

test('Vite reenvia /api al puerto 8080 de Spring Boot', () => {
  const config = read('../vite.config.js')
  assert.match(config, /['"]\/api['"]\s*:/)
  assert.match(config, /http:\/\/127\.0\.0\.1:8080/)
})

test('Axios usa /api por defecto sin requerir un origen CORS', () => {
  const cliente = read('../src/api/http.js')
  assert.match(cliente, /VITE_API_URL\?\.trim\(\)\s*\|\|\s*['"]\/api['"]/) 
  assert.match(read('../.env.example'), /VITE_API_URL=\/api/)
})

test('La configuracion CORS admite origenes exactos sin permitir todos', () => {
  const java = read('../../ServicesBackend/src/main/java/pe/fullstack/servicesbackend/portal/config/Seguridad.java')
  assert.match(java, /setAllowedOrigins\(Arrays\.stream/)
  assert.doesNotMatch(java, /setAllowedOrigins\(List\.of\("\*"\)\)/)
})
