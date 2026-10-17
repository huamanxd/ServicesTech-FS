#!/usr/bin/env python3
"""Prueba real: Nginx -> Spring Boot -> PostgreSQL -> Flyway + roles."""
import json
import os
from urllib.request import Request, urlopen
from urllib.error import HTTPError

base = os.environ.get('BASE_URL', 'http://127.0.0.1:8088').rstrip('/')

def api(method, path, body=None, token=None, expected=200):
    headers = {'Accept': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    if body is not None:
        headers['Content-Type'] = 'application/json'
    request = Request(base + path, data=json.dumps(body).encode() if body is not None else None,
                      headers=headers, method=method)
    try:
        with urlopen(request, timeout=15) as response:
            status, raw = response.status, response.read()
    except HTTPError as error:
        status, raw = error.code, error.read()
    assert status == expected, f'{method} {path}: HTTP {status} (esperado {expected}): {raw[:300]!r}'
    if not raw:
        return None
    return json.loads(raw)

catalogo = api('GET', '/api/servicios')
assert isinstance(catalogo, list) and len(catalogo) >= 12, 'Catálogo inicial ausente'
print('OK: catálogo precargado con Flyway y visible por proxy de Nginx')

admin = api('POST', '/api/auth/personal', {
    'email': os.environ.get('ADMIN_EMAIL', 'admin@admin.com'),
    'clave': os.environ.get('ADMIN_PASSWORD', 'admin123456789')
})
assert admin['usuario']['rol'] == 'ADMIN'
assert len(api('GET', '/api/admin/servicios', token=admin['token'])) >= 12
print('OK: login exclusivo de admin y menú/API de administración')

api('GET', '/api/admin/servicios', expected=401)
cliente = api('POST', '/api/auth/registro', {
    'nombres': 'Cliente', 'apellidos': 'Pruebas', 'email': 'cliente-ci@example.test',
    'telefono': '987654321', 'clave': 'ClavePrueba2026!'
}, expected=201)
assert cliente['usuario']['rol'] == 'CLIENTE'
api('GET', '/api/admin/servicios', token=cliente['token'], expected=403)
api('POST', '/api/auth/personal', {
    'email': 'cliente-ci@example.test', 'clave': 'ClavePrueba2026!'
}, expected=401)
print('OK: permisos del cliente separados de los del administrador')

for route in ('/', '/admin', '/cliente', '/catalogo'):
    with urlopen(base + route, timeout=15) as response:
        html = response.read().decode('utf-8')
        assert response.status == 200 and '<html' in html.lower(), f'SPA falló: {route}'
print('OK: React Router SPA servido por Nginx')
print('Prueba de integración completada')
