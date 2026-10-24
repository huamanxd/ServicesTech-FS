# ServicesTech

Plataforma para empresas de servicio técnico: el **cliente** registra solicitudes y sigue su atención, el **administrador** las aprueba, genera órdenes y asigna técnicos, y el **técnico** atiende y documenta la solución. Los dashboards muestran estados, retrasos, tiempos, cumplimiento, rendimiento técnico y satisfacción.

Requerimientos: `Taller1/Taller1_ServiceTech_vF.md` · Modelo de datos: [`docs/MODELO_DATOS.md`](docs/MODELO_DATOS.md)

| Capa | Tecnología | Despliegue |
|---|---|---|
| Frontend | React 19, Vite, Tailwind CSS 4, React Router, Axios | Vercel (o Nginx en Docker) |
| Backend | Java 21, Spring Boot 4, Spring Security (JWT), JPA, Flyway, MapStruct, Lombok, springdoc (Swagger) | Render (Docker) |
| Base de datos | PostgreSQL 17 | Render PostgreSQL (o contenedor) |
| CI/CD | GitHub Actions, GitHub Packages (GHCR) | — |

## Funcionalidades

| Requerimiento | Dónde |
|---|---|
| Registro e inicio de sesión por rol (cliente, técnico, administrador) | `/registro` (solo clientes), `/acceso/cliente`, `/acceso/personal` |
| Solicitudes con asunto, prioridad y estado | Cliente → *Nueva solicitud* |
| Revisión y aprobación; conversión en orden con detalle de servicios | Admin → *Solicitudes* |
| Asignación de técnicos y cambios de estado con bitácora | Admin → *Órdenes*; Técnico → *Mis órdenes* |
| El técnico ve las órdenes ingresadas y toma una | Técnico → *Bandeja de órdenes* |
| Registro de la solución y evaluación de satisfacción (1–5) | Técnico registra la solución; el cliente califica |
| Historial y seguimiento del cliente (solo su información) | Cliente → *Mis solicitudes* → *Ver seguimiento* |
| Dashboard del cliente: estados, tiempos, cumplimiento | Cliente → *Resumen* |
| Dashboard administrativo: órdenes, retrasos, tiempos, rendimiento técnico, cumplimiento, satisfacción | Admin → *Dashboard* |
| Solo un administrador crea técnicos o administradores | Admin → *Usuarios* |
| Activar o desactivar usuarios (nunca se eliminan) | Admin → *Usuarios* |
| Catálogo de servicios con precios referenciales en soles | `/catalogo`; Admin → *Catálogo* |

> **Interpretación:** «el técnico ve las solicitudes ingresadas y toma la solicitud» se implementa como una **bandeja** con las órdenes que el administrador generó a partir de solicitudes aprobadas y que aún no tienen técnico. Así se respeta el flujo del caso de negocio (el administrador aprueba y genera la orden; el técnico la atiende).

## Flujo

```
Cliente            Administrador                    Técnico                     Cliente
SOLICITUD  ──►  revisa y APRUEBA  ──►  ORDEN  ──►  toma o recibe la orden  ──►  califica
(PENDIENTE)     genera la orden        (PENDIENTE)  EN_ATENCION → RESUELTA      → CERRADA
                y su detalle                        registra la solución
```

Las transiciones permitidas y las definiciones de los indicadores están en [`docs/MODELO_DATOS.md`](docs/MODELO_DATOS.md).

## Estructura

```text
ServicesTech-FS/
├── ServicesBackend/                Spring Boot (Gradle Kotlin DSL)
│   ├── src/main/java/pe/fullstack/servicesbackend/
│   │   ├── config/                 Seguridad, filtro JWT, errores, OpenAPI, admin inicial, DATABASE_URL
│   │   ├── controlador/            REST: acceso, catálogo, cliente, técnico, admin
│   │   ├── dto/                    Peticiones (validadas), Respuestas y VistaMapper (MapStruct)
│   │   ├── modelo/                 Entidades JPA del ERD
│   │   ├── repositorio/            Spring Data JPA
│   │   └── servicio/               Reglas de negocio, estados, métricas y dashboards
│   ├── src/main/resources/db/migration/   Flyway V1, V2, V3
│   └── Dockerfile
├── ServicesFrontend/               React + Vite
│   ├── src/api/                    Axios (token, 401, errores) y endpoints
│   ├── src/pantallas/{cliente,tecnico,admin}/   Paneles por rol
│   ├── src/componentes/            Cabecera, rutas protegidas, UI compartida
│   ├── tests/                      Vitest + Testing Library
│   ├── nginx.conf · Dockerfile · vercel.json
├── docs/MODELO_DATOS.md
├── scripts/smoke-ci.py             Prueba E2E del flujo completo
├── docker-compose.yml              Local: construye las 3 capas
├── compose.deploy.yml              Servidor propio: imágenes de GHCR
├── render.yaml                     Blueprint de Render (backend + PostgreSQL)
└── .github/workflows/ci-cd.yml
```

## Ejecutar con Docker Compose

Requisito: Docker Desktop (Windows/macOS) o Docker Engine con Compose v2.

```bash
cp .env.example .env            # PowerShell: Copy-Item .env.example .env
docker compose up -d --build --wait
```

| Recurso | URL |
|---|---|
| Aplicación | http://localhost:8088 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Salud del backend | http://localhost:8080/actuator/health |
| PostgreSQL (solo desde tu equipo) | `127.0.0.1:5432` |

Administrador de demostración (solo local): `admin@admin.com` / `admin123456789`, en `/acceso/personal`. Desde *Usuarios* crea un técnico para probar el flujo completo.

- `docker compose down` detiene sin borrar datos. `docker compose down -v` **borra** el volumen de PostgreSQL.
- Si un puerto está ocupado, cambia `FRONTEND_PORT`, `BACKEND_PORT` o `POSTGRES_PORT` en `.env`.

## Desarrollo sin Docker

```bash
# 1. Base de datos (o un PostgreSQL propio)
docker compose up -d postgres

# 2. Backend (Java 21). PowerShell: $env:DB_PASSWORD="..." y .\gradlew.bat bootRun
cd ServicesBackend
export DB_PASSWORD=solo_desarrollo_123 APP_JWT_SECRET=$(openssl rand -base64 32) \
       ADMIN_EMAIL=admin@admin.com ADMIN_PASSWORD=admin123456789
./gradlew bootRun

# 3. Frontend (Node 22). Vite reenvía /api a http://127.0.0.1:8080
cd ServicesFrontend
npm ci
npm run dev                       # http://localhost:5173
```

### Variables de entorno del backend

| Variable | Obligatoria | Descripción |
|---|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Sí (o `DATABASE_URL`) | Conexión JDBC a PostgreSQL. |
| `DATABASE_URL` | Alternativa | Formato `postgres://usuario:clave@host:puerto/base` (Render); se traduce a JDBC. |
| `APP_JWT_SECRET` | Sí | Base64 de ≥ 32 bytes (`openssl rand -base64 32`). |
| `ADMIN_EMAIL`, `ADMIN_PASSWORD` | Recomendadas | Crean el primer administrador si el correo no existe (clave ≥ 12 caracteres). Nunca modifican una cuenta existente. |
| `FRONTEND_ORIGINS` | En Render | Orígenes permitidos por CORS, separados por coma (ej. `https://servicestech.vercel.app`). También se acepta el nombre anterior `FRONTEND_ORIGIN`. |
| `APP_JWT_HOURS` | No | Duración de la sesión (8 h por defecto). |
| `API_DOCS_ENABLED` | No | `false` oculta Swagger. |
| `PORT` | No | Render la define automáticamente. |

## API

Documentación interactiva en **`/swagger-ui.html`** (botón *Authorize* con el token que devuelve el login). Errores siempre en el formato `{"message": "...", "fields": {"campo": "detalle"}}`: 400 validación, 401 sin sesión, 403 sin permiso, 404 no existe, 409 conflicto o transición no permitida.

| Rol | Endpoints |
|---|---|
| Público | `GET /api/servicios`, `GET /api/prioridades`, `POST /api/auth/registro`, `POST /api/auth/cliente`, `POST /api/auth/personal` |
| Con sesión | `GET /api/auth/perfil`, `POST /api/auth/salir` |
| Cliente | `GET /api/cliente/dashboard` · `GET/POST /api/cliente/solicitudes` · `POST /api/cliente/solicitudes/{id}/cancelar` · `GET /api/cliente/ordenes/{id}/historial` · `POST /api/cliente/ordenes/{id}/satisfaccion` |
| Técnico | `GET /api/tecnico/dashboard` · `GET /api/tecnico/ordenes` · `GET /api/tecnico/ordenes/disponibles` · `POST /api/tecnico/ordenes/{id}/tomar` · `POST …/iniciar` · `POST …/solucion` · `GET …/historial` |
| Administrador | `GET /api/admin/dashboard` · `GET /api/admin/solicitudes?estado=` · `POST /api/admin/solicitudes/{id}/revision` · `POST /api/admin/solicitudes/{id}/orden` · `GET /api/admin/ordenes?estado=` · `POST /api/admin/ordenes/{id}/asignar` · `POST …/cerrar` · `POST …/cancelar` · `GET …/historial` · `GET/POST/PUT/DELETE /api/admin/servicios` · `GET/POST /api/admin/usuarios` · `PATCH /api/admin/usuarios/{id}/estado` |

## Pruebas

```bash
cd ServicesBackend && ./gradlew test     # unitarias + integración (requiere PostgreSQL y DB_*)
cd ServicesFrontend && npm run lint && npm test && npm run build
BASE_URL=http://localhost:8088 python3 scripts/smoke-ci.py   # E2E contra el stack levantado
```

- **Backend:** reglas de estado, métricas, JWT, traducción de `DATABASE_URL`, reglas de acceso (Mockito) y un flujo completo con MockMvc contra PostgreSQL real.
- **Frontend:** validaciones, utilidades, rutas protegidas y login (Vitest + Testing Library).
- **E2E (`smoke-ci.py`):** registro → solicitud → aprobación → orden → técnico → solución → calificación → dashboards → desactivación, con verificación de permisos.

## CI/CD y despliegue

`.github/workflows/ci-cd.yml` se ejecuta en cada push o PR a `main`:

| Job | Qué hace |
|---|---|
| `backend-test` | Java 21 + PostgreSQL de servicio: `./gradlew test`. |
| `frontend-test` | `npm ci`, lint, pruebas y build. |
| `docker-smoke` | `docker compose up --build` y `scripts/smoke-ci.py` contra Nginx. |
| `publish` | Fuera de los PR: publica `ghcr.io/<usuario>/servicestech-backend` y `-frontend`. |
| `deploy-render` | En `main`: dispara el deploy hook de Render si existe y verifica la salud del backend. |
| `deploy-vps` | En `main`, solo si la variable `DEPLOY_ENABLED=true`: por SSH hace `git pull` y `docker compose up --build` en tu servidor (secretos `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_PATH`, `DEPLOY_SSH_PRIVATE_KEY`, `DEPLOY_KNOWN_HOSTS`). |

### Backend en Render

**Opción Blueprint (servicio nuevo):** *Render → New → Blueprint* y selecciona el repositorio. `render.yaml` crea el servicio Docker `servicestech-fs` y la base `servicestech-db`, genera `APP_JWT_SECRET`, conecta `DATABASE_URL` y pide `ADMIN_EMAIL`, `ADMIN_PASSWORD` y `FRONTEND_ORIGINS`.

**Servicio existente:** en *Environment* define `DATABASE_URL` (o `DB_URL`/`DB_USER`/`DB_PASSWORD` en formato JDBC), `APP_JWT_SECRET`, `ADMIN_EMAIL`, `ADMIN_PASSWORD` y `FRONTEND_ORIGINS`. En *Settings*: Root Directory `ServicesBackend`, Health Check Path `/actuator/health`.

**Despliegue continuo**, elige una opción:
- **A (recomendada):** *Settings → Auto-Deploy → After CI Checks Pass*. Render despliega solo si el workflow pasa.
- **B:** Auto-Deploy en *Off* y guarda el *Deploy Hook* como secreto `RENDER_DEPLOY_HOOK_URL` en GitHub.

Opcional: la variable de repositorio `RENDER_BACKEND_URL` (ej. `https://servicestech-fs.onrender.com`) hace que el job espere a que el backend responda.

> En el plan gratuito, el servicio se duerme tras 15 minutos sin tráfico y tarda ~1 minuto en despertar (el frontend espera hasta 70 s). La base PostgreSQL gratuita vence a los 30 días.

### Frontend en Vercel

*Add New → Project →* importa el repositorio con **Root Directory = `ServicesFrontend`** (Vite se detecta solo). `vercel.json` reenvía `/api/*` a `https://servicestech-fs.onrender.com`; si tu servicio de Render tiene otra URL, cámbiala ahí. Vercel despliega en cada push con su integración de Git y crea *previews* en los PR. Agrega el dominio de Vercel a `FRONTEND_ORIGINS` en Render.

### Servidor propio con Docker

```bash
cp .env.production.example .env     # edita todos los valores
docker login ghcr.io                # si los paquetes son privados (token con read:packages)
docker compose -f compose.deploy.yml up -d --pull always --wait
```

Usa las imágenes que publica el CI. Para volver a una versión anterior cambia `IMAGE_TAG` por una etiqueta `sha-…` o `v…`. Pon delante un proxy con HTTPS.

## Seguridad

- La autorización por rol se hace en Spring Security (`Seguridad.java`); lo que no está declarado queda denegado. Las rutas de React solo ocultan pantallas.
- Contraseñas con BCrypt. JWT HS256 firmado con `APP_JWT_SECRET`, cuyo hash SHA-256 se guarda en `sesiones`: cerrar sesión o desactivar la cuenta revoca el acceso al instante. Las sesiones vencidas se limpian cada hora.
- Los mensajes de inicio de sesión no revelan si un correo existe.
- El cliente solo accede a lo suyo: el id sale del token, nunca del formulario ni de la URL.
- Pendiente para producción real: limitar intentos de login, recuperar contraseña por correo y usar cookies HttpOnly en lugar de `sessionStorage`.

## Solución de problemas

| Síntoma | Causa probable |
|---|---|
| `docker compose` dice «Define APP_JWT_SECRET» | Falta `.env`: `cp .env.example .env`. |
| El backend no arranca: `APP_JWT_SECRET debe tener al menos 32 bytes` | Genera uno con `openssl rand -base64 32`. |
| El admin no puede entrar | `ADMIN_*` solo se usan en el primer arranque: el administrador inicial nunca se actualiza. |
| En Vercel la primera carga tarda o da timeout | El backend gratuito de Render estaba dormido; reintenta tras unos segundos. |
| 403 «Invalid CORS request» con Vercel | En Render, `FRONTEND_ORIGINS` debe incluir el dominio exacto de Vercel (p. ej. `https://servicestech.vercel.app`; admite comodines como `https://servicestech-*.vercel.app` para las vistas previas). |
| 403 «Invalid CORS request» con Docker (PC o servidor) | Imagen del frontend anterior a la corrección de `nginx.conf`: reconstruye con `docker compose up -d --build`. |
| `./gradlew: /bin/sh^M` | Finales de línea CRLF: `.gitattributes` lo evita; vuelve a hacer checkout. |
