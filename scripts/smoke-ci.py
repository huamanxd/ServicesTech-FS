#!/usr/bin/env python3
"""Prueba de humo E2E del flujo completo de ServicesTech contra una instancia real.

Cliente registra solicitud -> admin aprueba y genera la orden -> técnico la toma,
la atiende y registra la solución -> cliente califica (la orden se cierra) -> dashboards.

Uso:
  BASE_URL=http://127.0.0.1:8088 ADMIN_EMAIL=... ADMIN_PASSWORD=... python3 scripts/smoke-ci.py
  CHECK_SPA=1 además comprueba que Nginx sirva las rutas de React.
"""
import json
import os
import random
from urllib.error import HTTPError
from urllib.request import Request, urlopen

BASE = os.environ.get('BASE_URL', 'http://127.0.0.1:8088').rstrip('/')
ADMIN_EMAIL = os.environ.get('ADMIN_EMAIL', 'admin@admin.com')
ADMIN_PASSWORD = os.environ.get('ADMIN_PASSWORD', 'admin123456789')
SUFIJO = f'{random.randint(0, 99_999_999):08d}'


def api(method, path, body=None, token=None, expected=200, raw_body=None):
    headers = {'Accept': 'application/json'}
    if token:
        headers['Authorization'] = 'Bearer ' + token
    data = None
    if body is not None or raw_body is not None:
        headers['Content-Type'] = 'application/json'
        data = raw_body.encode() if raw_body is not None else json.dumps(body).encode()
    request = Request(BASE + path, data=data, headers=headers, method=method)
    try:
        with urlopen(request, timeout=20) as response:
            status, raw = response.status, response.read()
    except HTTPError as error:
        status, raw = error.code, error.read()
    assert status == expected, f'{method} {path}: HTTP {status} (esperado {expected}): {raw[:400]!r}'
    return json.loads(raw) if raw else None


def ok(mensaje):
    print('OK:', mensaje)


# 1. Catálogo público -----------------------------------------------------------
servicios = api('GET', '/api/servicios')
assert len(servicios) >= 12, 'Catálogo inicial ausente'
prioridades = {p['nombre']: p for p in api('GET', '/api/prioridades')}
assert set(prioridades) == {'BAJA', 'MEDIA', 'ALTA', 'CRITICA'}
ok('catálogo y prioridades cargados por Flyway')

# 2. Seguridad básica -------------------------------------------------------------
api('GET', '/api/admin/dashboard', expected=401)
api('POST', '/api/auth/cliente', raw_body='{"email":', expected=400)
admin = api('POST', '/api/auth/personal', {'email': ADMIN_EMAIL, 'clave': ADMIN_PASSWORD})
assert 'ADMINISTRADOR' in admin['usuario']['roles']
A = admin['token']
ok('login del administrador; 401 sin token y 400 con JSON inválido')

# 3. Cliente ------------------------------------------------------------------------
cliente = api('POST', '/api/auth/registro', {
    'nombres': 'Cliente', 'apellidos': 'Pruebas', 'dni': SUFIJO,
    'email': f'cliente-{SUFIJO}@example.test', 'telefono': '987654321', 'clave': 'ClavePrueba2026!'
}, expected=201)
assert cliente['usuario']['roles'] == ['CLIENTE']
C = cliente['token']
api('GET', '/api/admin/dashboard', token=C, expected=403)
api('POST', '/api/auth/personal', {'email': f'cliente-{SUFIJO}@example.test', 'clave': 'ClavePrueba2026!'}, expected=403)
solicitud = api('POST', '/api/cliente/solicitudes', {
    'servicioId': servicios[0]['id'], 'prioridadId': prioridades['ALTA']['id'],
    'asunto': 'Laptop no enciende', 'descripcion': 'La laptop no enciende desde ayer por la tarde',
    'direccion': 'Lima'
}, token=C, expected=201)
assert solicitud['estado'] == 'PENDIENTE' and solicitud['orden'] is None
ok('cliente registrado, separado del portal de personal, solicitud PENDIENTE')

# 4. Administrador: revisión, técnico y orden ----------------------------------------
sid = solicitud['id']
assert api('POST', f'/api/admin/solicitudes/{sid}/revision', {'estado': 'APROBADA'}, token=A)['estado'] == 'APROBADA'
tecnico = api('POST', '/api/admin/usuarios', {
    'nombres': 'Tecnico', 'apellidos': 'Pruebas', 'dni': f'{(int(SUFIJO) + 1) % 100_000_000:08d}',
    'email': f'tecnico-{SUFIJO}@example.test', 'telefono': '987654322', 'clave': 'ClaveTecnico2026!',
    'rol': 'TECNICO'
}, token=A, expected=201)
orden = api('POST', f'/api/admin/solicitudes/{sid}/orden', {'observaciones': 'Revisar fuente'}, token=A, expected=201)
assert orden['estado'] == 'PENDIENTE' and orden['tecnico'] is None and len(orden['servicios']) == 1
oid = orden['id']
api('POST', f'/api/admin/ordenes/{oid}/cerrar', token=A, expected=409)
ok('solicitud aprobada, técnico creado por el admin, orden PENDIENTE; transición inválida -> 409')

# 5. Técnico -------------------------------------------------------------------------
T = api('POST', '/api/auth/personal', {'email': f'tecnico-{SUFIJO}@example.test', 'clave': 'ClaveTecnico2026!'})['token']
assert any(o['id'] == oid for o in api('GET', '/api/tecnico/ordenes/disponibles', token=T))
assert api('POST', f'/api/tecnico/ordenes/{oid}/tomar', token=T)['estado'] == 'ASIGNADA'
assert api('POST', f'/api/tecnico/ordenes/{oid}/iniciar', token=T)['estado'] == 'EN_ATENCION'
resuelta = api('POST', f'/api/tecnico/ordenes/{oid}/solucion',
               {'descripcion': 'Se reemplazó la fuente de poder y se probó el equipo'}, token=T)
assert resuelta['estado'] == 'RESUELTA' and resuelta['solucion'] is not None
api('GET', '/api/cliente/solicitudes', token=T, expected=403)
ok('técnico toma la orden de la bandeja, la atiende y registra la solución')

# 6. Cliente califica -----------------------------------------------------------------
assert api('GET', '/api/cliente/dashboard', token=C)['pendientesDeCalificar'] == 1
cerrada = api('POST', f'/api/cliente/ordenes/{oid}/satisfaccion', {'calificacion': 5, 'comentario': 'Excelente'}, token=C)
assert cerrada['estado'] == 'CERRADA' and cerrada['satisfaccion']['calificacion'] == 5
estados = [h['estado'] for h in api('GET', f'/api/cliente/ordenes/{oid}/historial', token=C)]
assert estados == ['PENDIENTE', 'ASIGNADA', 'EN_ATENCION', 'RESUELTA', 'CERRADA'], estados
mias = api('GET', '/api/cliente/solicitudes', token=C)
assert len(mias) == 1 and mias[0]['orden']['estado'] == 'CERRADA'
ok('cliente califica, la orden queda CERRADA y el historial registra cada estado')

# 7. Dashboards ---------------------------------------------------------------------------
tablero = api('GET', '/api/admin/dashboard', token=A)
assert tablero['indicadores']['evaluaciones'] >= 1 and tablero['indicadores']['satisfaccionPromedio'] is not None
fila = next(t for t in tablero['tecnicos'] if t['tecnico']['id'] == tecnico['id'])
assert fila['resueltas'] == 1 and fila['satisfaccionPromedio'] == 5.0
assert api('GET', '/api/tecnico/dashboard', token=T)['indicadores']['resueltas'] == 1
assert api('GET', '/api/cliente/dashboard', token=C)['indicadores']['cumplimientoPct'] == 100.0
ok('dashboards de administrador, técnico y cliente')

# 8. Activar / desactivar -----------------------------------------------------------------
assert api('PATCH', f'/api/admin/usuarios/{tecnico["id"]}/estado', {'activo': False}, token=A)['activo'] is False
api('GET', '/api/tecnico/dashboard', token=T, expected=401)
api('POST', '/api/auth/personal', {'email': f'tecnico-{SUFIJO}@example.test', 'clave': 'ClaveTecnico2026!'}, expected=403)
api('PATCH', f'/api/admin/usuarios/{admin["usuario"]["id"]}/estado', {'activo': False}, token=A, expected=409)
ok('desactivar revoca sesiones e impide el acceso; el admin no puede desactivarse a sí mismo')

api('POST', '/api/auth/salir', token=C)
api('GET', '/api/cliente/solicitudes', token=C, expected=401)
ok('cierre de sesión revoca el token')

if os.environ.get('CHECK_SPA') == '1':
    for ruta in ('/', '/catalogo', '/cliente', '/tecnico', '/admin', '/acceso/personal'):
        with urlopen(BASE + ruta, timeout=15) as response:
            html = response.read().decode('utf-8')
            assert response.status == 200 and '<div id="root">' in html, f'SPA falló: {ruta}'
    ok('Nginx sirve las rutas de React Router')

print('Prueba de humo completada')
