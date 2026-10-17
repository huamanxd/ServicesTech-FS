import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
const read = path => readFileSync(new URL(path, import.meta.url), 'utf8')

test('Docker inicializa el admin solicitado solo por variables de entorno', () => {
  const env = read('../../.env.example')
  const compose = read('../../docker-compose.yml')
  assert.match(env, /ADMIN_EMAIL=admin@admin\.com/)
  assert.match(env, /ADMIN_PASSWORD=admin123456789/)
  assert.match(compose, /ADMIN_PASSWORD:\s*\$\{ADMIN_PASSWORD:/)
})

test('Nginx enruta la API a Spring Boot y protege rutas SPA de React', () => {
  const nginx = read('../nginx.conf')
  assert.match(nginx, /location \/api\//)
  assert.match(nginx, /proxy_pass http:\/\/backend:8080;/)
  assert.match(nginx, /try_files \$uri \$uri\/ \/index\.html;/)
})

test('CI publica dos imagenes GHCR tras pruebas y valida acceso por roles', () => {
  const pipeline = read('../../.github/workflows/ci-cd.yml')
  assert.match(pipeline, /needs: \[docker-smoke\]/)
  assert.match(pipeline, /- part: backend/)
  assert.match(pipeline, /- part: frontend/)
  assert.match(pipeline, /ghcr\.io\/\$\{\{ steps\.names\.outputs\.owner \}\}/)
  assert.match(read('../../scripts/smoke-ci.py'), /expected=403/)
})

