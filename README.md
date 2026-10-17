# ServicesTech — sistema de servicios técnicos con Docker, PostgreSQL, Spring Boot y React

## 1. ¿Qué es ServicesTech?

ServicesTech es un portal de atención y soporte técnico dirigido a clientes en Perú. Permite revisar un catálogo de servicios con **precios referenciales en soles**, registrar cuentas de clientes, solicitar servicios y consultar el estado de las solicitudes. El personal autorizado dispone de un **panel administrativo independiente** para gestionar solicitudes, editar el catálogo y crear cuentas de trabajadores. Los precios son orientativos y no reemplazan una cotización final.

**Se conserva la estructura del proyecto original:** `ServicesBackend/` contiene Java 21, Spring Boot 4.1.1, Gradle Kotlin DSL, Flyway y PostgreSQL; `ServicesFrontend/` contiene React, React Router, Axios, Tailwind CSS y Vite. El frontend conserva `Home.jsx`, `Cabecera.jsx`, `Cuerpo.jsx`, `Piedepagina.jsx`, `Login.jsx`, `Signup.jsx`, `Catalogo.jsx` y los paneles existentes.

```text
ServicesTech/
├── ServicesBackend/
│   ├── Dockerfile                  # Compila Gradle y ejecuta el JAR Java 21
│   ├── .dockerignore
│   ├── build.gradle.kts             # Incluye Flyway + Actuator de salud
│   └── src/main/resources/db/migration/
│       ├── V1__create_portal.sql
│       └── V2__catalogo_referencial_peru.sql
├── ServicesFrontend/
│   ├── Dockerfile                  # Compila Vite y sirve el frontend con Nginx
│   ├── nginx.conf                  # /api -> backend:8080 y fallback React Router
│   ├── .dockerignore
│   └── src/
├── .github/workflows/ci-cd.yml    # CI pruebas + integración + GHCR + CD opcional
├── scripts/smoke-ci.py            # Prueba real del stack y permisos por rol
├── docker-compose.yml             # Desarrollo: construye 3 servicios localmente
├── compose.deploy.yml             # Servidor: obtiene 2 imágenes de GitHub Packages
├── .env.example                   # Configuración de demostración local
├── .env.production.example        # Plantilla, sin contraseñas reales, para VPS
└── README.md
```

### Arquitectura de ejecución

```text
Navegador http://localhost:8088
             │
      Nginx + React (frontend)
             ├── /*       -> archivos HTML/JS + rutas SPA
             └── /api/*   -> proxy a backend:8080 (red interna Docker)
                                    │
                              Spring Boot 4
                                    │
                      JPA + Flyway + PostgreSQL 17
                                    │
                           Volumen persistente
```

El navegador no necesita acceder directamente al backend: **Axios usa `/api`**, con el mismo origen que React; Nginx reenvía la petición al contenedor Spring Boot. No uses `localhost:8080` como URL Axios dentro de la imagen publicada. `localhost` dentro del contenedor sería el contenedor mismo, no el backend.

## 2. Ejecutar las tres capas con un comando (Docker Compose)

Requisitos: Docker Desktop (Windows/macOS) o Docker Engine y plugin Compose V2 (Linux); conexión a internet para descargar las imágenes y dependencias la primera vez. **No hace falta instalar Java, Node, Gradle ni PostgreSQL en el equipo anfitrión.**

Abre una terminal en la raíz `ServicesTech/` (al lado de `docker-compose.yml`):

**macOS/Linux:**

```bash
cp .env.example .env
docker compose up -d --build --wait
```

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
docker compose up -d --build --wait
```

Esto construye la imagen de Spring Boot con el Wrapper de Gradle, construye React con Node 22 y publica los archivos mediante Nginx, y arranca PostgreSQL 17. Compose espera la salud de PostgreSQL y después la del backend antes de arrancar el frontend. **Flyway crea automáticamente las 4 tablas** `usuarios`, `sesiones`, `servicios`, `solicitudes` y precarga 12 servicios. El historial está en `flyway_schema_history`.

| Recurso | Dirección en desarrollo local | Comentario |
|---|---|---|
| Página web React + API con proxy | `http://localhost:8088` | Acceso principal |
| Catálogo desde la web | `http://localhost:8088/api/servicios` | Debe devolver JSON |
| Spring Boot, solo desde el propio equipo | `http://localhost:8080` | El proxy normalmente es preferible |
| Estado del backend | `http://localhost:8080/actuator/health` | Devuelve `UP` cuando está listo |
| PostgreSQL, solo desde el propio equipo | `127.0.0.1:5432` | Para pgAdmin; no exponer públicamente |

Si el puerto `8088`, `8080` o `5432` ya está ocupado, cambia `FRONTEND_PORT`, `BACKEND_PORT` o `POSTGRES_PORT` en el **`.env` de la raíz** y reinicia con `docker compose up -d`. El puerto interno entre contenedores **siempre es** `postgres:5432` y `backend:8080`.

Para revisar:

```bash
docker compose ps
docker compose logs -f backend
curl -i http://localhost:8088/api/servicios
curl -i http://localhost:8080/actuator/health
```

Para detener sin borrar datos: `docker compose down`. **No ejecutes `docker compose down -v`** si tienes información importante: `-v` elimina el volumen de PostgreSQL. Si ya usabas el proyecto `ServicesTech_Final`, la plantilla local establece `COMPOSE_PROJECT_NAME=servicestech_final` para intentar reutilizar sus contenedores y su volumen existentes. Si tu proyecto Compose tenía otro nombre, revisa `docker volume ls` y ajusta `COMPOSE_PROJECT_NAME` antes de iniciar: cambiar ese nombre crea/selecciona **otro volumen** y no transfiere los datos por sí solo.

### Usuario administrador de demostración

| Campo | Valor solicitado (solo desarrollo) |
|---|---|
| Correo | **`admin@admin.com`** |
| Contraseña inicial | **`admin123456789`** |
| Pantalla exclusiva | `http://localhost:8088/acceso/personal` |
| Panel exclusivo | `http://localhost:8088/admin` |

Estas credenciales se incluyen **solamente en `.env.example` para pruebas locales**. Copia a `.env` e inicializa la aplicación. **La contraseña se guarda como hash BCrypt en PostgreSQL, no en texto plano.** `AdministradorInicial.java` crea la cuenta con rol `ADMIN` **únicamente si ese correo no existe**. No cambia contraseñas ni roles de cuentas previas. El login del cliente `/acceso/cliente` rechaza administradores, mientras que `/acceso/personal` rechaza clientes. El menú exclusivo `PanelAdmin.jsx` incluye *Solicitudes, Catálogo, Trabajadores*; la API `/api/admin/**` está protegida por Spring Security y el menú/ruta `/admin` por React Router.

**Advertencia de seguridad:** `admin123456789` es una clave pública y débil. **NO la uses en un servidor accesible por Internet**. Define una contraseña única y robusta en el `.env` privado del servidor ANTES del primer arranque. Si ya se creó la cuenta con la clave de ejemplo, cambiar `ADMIN_PASSWORD` en `.env` **no modifica la contraseña del usuario existente**. Para reemplazarla debes implementar un procedimiento administrativo de restablecimiento, con autenticación y auditoría; no borres la base de datos.

## 3. CI: pruebas y dos paquetes Docker visibles en GitHub

El workflow está en **`.github/workflows/ci-cd.yml`** y corre al crear un Pull Request a `main`, al hacer `git push` a `main`, al crear etiquetas `v*` y manualmente desde **Actions → ServicesTech - CI / GitHub Packages / CD → Run workflow**.

| Trabajo en Actions | Qué hace |
|---|---|
| `backend-test` | Java 21, PostgreSQL 17 en el runner, `./gradlew clean test` (JUnit/JPA) |
| `frontend-test` | Node.js 22, `npm install`, `npm test`, `npm run build` |
| `docker-smoke` | `docker compose up -d --build --wait`; comprueba JSON del catálogo, cuenta admin, rechazo de clientes en `/api/admin`, y React Router; después limpia su volumen **efímero de CI** |
| `publish` | Si todo pasa y NO es un PR, publica **dos imágenes separadas** a GitHub Container Registry (GHCR) |
| `deploy` | En `main`, despliega al VPS **solo si** has establecido la variable `DEPLOY_ENABLED=true` y preparado SSH/servidor |

**Paquetes en GitHub:** reemplaza `<TU_USUARIO>` por tu usuario/organización GitHub **en minúsculas**:

```text
ghcr.io/<TU_USUARIO>/servicestech-backend:main
ghcr.io/<TU_USUARIO>/servicestech-frontend:main
```

También publica la etiqueta `latest`, una etiqueta `sha-...` y, cuando lanzas `v1.0.0`, imágenes con esa versión. Son **paquetes OCI/Docker**, no archivos ZIP ni artefactos JavaScript sueltos. En GitHub se visualizan en el perfil de usuario u organización → **Packages**; también en la sección *Packages* del repositorio si quedan asociados a él. Normalmente los paquetes nuevos son privados; puedes ajustar visibilidad o permisos desde GitHub.

### Subir el proyecto a tu GitHub

Crea primero un repositorio vacío en GitHub, por ejemplo `ServicesTech` (no selecciones inicializar con README si vas a subir esta carpeta) y desde la raíz del proyecto:

```bash
git init
git branch -M main
git add .
git commit -m "ServicesTech: Docker Compose y CI/CD GHCR"
git remote add origin https://github.com/TU_USUARIO/ServicesTech.git
git push -u origin main
```

Si ya tienes Git inicializado, omite `git init` y configura tu remoto existente; confirma con `git remote -v`. **No publiques el archivo `.env` ni claves SSH**. En **Settings → Actions → General** verifica que Actions esté habilitado y que las acciones estándar (GitHub, Docker y Gradle) estén permitidas. El job `publish` usa el `GITHUB_TOKEN` automático con permiso `packages: write`, por lo que no requiere un token personal para publicar en GHCR. Si un paquete existe previamente, revisa su **Manage Actions access / Manage access** y da permisos de escritura al repositorio.

**Limitación de reproducibilidad:** el frontend recibido no incluía `package-lock.json`. Por eso el workflow usa `npm install` en lugar de `npm ci`; necesita conexión al registro npm y las versiones transitivas podrían variar. Cuando tengas acceso a internet, ejecuta `cd ServicesFrontend && npm install` y agrega el `package-lock.json` generado a Git; después cambia los comandos `npm install` de Dockerfile/Actions por `npm ci` para builds completamente fijados. En el entorno donde preparé esta entrega no se pudo descargar npm para generar un lockfile fiable.

## 4. CD: desplegar automáticamente en un VPS con Docker

Requisito externo: disponer de un **VPS Linux** con Docker Engine, Docker Compose V2, acceso por SSH, cortafuegos configurado y, para producción real, un **dominio con HTTPS/TLS** usando Caddy, Traefik o Nginx con certificado. GitHub Actions no proporciona un servidor de producción: **debes configurar el VPS una vez**.

### Primera instalación en el servidor (una sola vez)

1. En tu VPS instala Docker + Compose, crea un usuario que pueda ejecutar `docker compose` y prepara la carpeta, por ejemplo `sudo mkdir -p /opt/servicestech && sudo chown "$USER":"$USER" /opt/servicestech`.
2. Copia **`compose.deploy.yml`** y **`.env.production.example`** al VPS mediante SCP (o desde un checkout del repositorio). Crea `/opt/servicestech/.env` copiando la plantilla y restringe permisos con `chmod 600 /opt/servicestech/.env`.
3. Edita `.env`: coloca `GHCR_OWNER=tu_usuario_github_en_minusculas`, `IMAGE_TAG=main`, contraseña aleatoria para `DB_PASSWORD`, `ADMIN_EMAIL=admin@admin.com` y **una contraseña admin distinta de `admin123456789` para producción**. `FRONTEND_PORT=80` publica el sitio en el puerto 80 del VPS.
4. Después de que Actions publique los paquetes, si GHCR es privado, inicia sesión **una vez en el VPS** mediante `docker login ghcr.io -u TU_USUARIO`, utilizando un token de GitHub con permiso `read:packages` (no la contraseña de tu cuenta). El despliegue SSH reutiliza ese login en el usuario remoto.
5. Ejecuta en `/opt/servicestech`: `docker compose -f compose.deploy.yml up -d --wait`. Se descargan **las mismas dos imágenes que se ven en GitHub Packages** y se crea PostgreSQL con volumen persistente. Comprueba `docker compose -f compose.deploy.yml ps`.

**ATENCIÓN HTTPS:** la plantilla expone HTTP para que puedas comprobar la primera instalación. **Antes de permitir usuarios reales, configura TLS/HTTPS y un proxy inverso en el servidor**; no envíes claves o tokens por HTTP en Internet. El frontend de React necesita TLS en el dominio público; la base de datos y Spring Boot no publican puertos externos en `compose.deploy.yml`.

### Habilitar despliegue automático desde GitHub

En el repositorio → **Settings → Secrets and variables → Actions**:

- Crea una **variable de repositorio** `DEPLOY_ENABLED` con valor `true` (sin ella `deploy` se omite y solo se publican paquetes).
- Crea **secrets de repositorio** (o secrets compatibles accesibles al job): `DEPLOY_HOST` (IP o dominio SSH), `DEPLOY_USER` (usuario con permiso para Docker), `DEPLOY_PATH` (por ejemplo `/opt/servicestech`), `DEPLOY_SSH_PRIVATE_KEY` (clave privada para ese usuario del VPS) y `DEPLOY_KNOWN_HOSTS` (línea del host SSH verificada mediante fingerprint). Agrega la clave pública correspondiente a `~/.ssh/authorized_keys` del VPS.
- Verifica la clave del host mediante un canal seguro; puedes obtener el formato inicial con `ssh-keyscan -H DOMINIO_O_IP` **y cotejar la huella del host antes de confiar en ella**. El workflow no desactiva `StrictHostKeyChecking`.
- Si usas **Settings → Environments → production**, puedes configurar revisores antes del despliegue.

Cuando hagas `git push origin main`, Actions ejecutará las pruebas, construirá las dos imágenes y, si todo pasa, actualizará el VPS copiando `compose.deploy.yml` y ejecutando `docker compose pull` + `up -d --wait`. **El `.env` con secretos permanece solo en el VPS** y nunca se sobrescribe. Las etiquetas `v*` también publican imágenes, pero el despliegue automático aquí se limita a `main`; para desplegar una versión específica cambia `IMAGE_TAG=v1.0.0` en el `.env` del VPS y ejecuta manualmente `docker compose -f compose.deploy.yml up -d --pull always --wait`.

Para hacer rollback cambia `IMAGE_TAG` por la etiqueta `sha-...` o `v...` anterior en el servidor y ejecuta `docker compose -f compose.deploy.yml up -d --pull always --wait`. **Advertencia:** no existe rollback automático de la estructura de datos; antes de migraciones futuras realiza un respaldo de PostgreSQL. Las migraciones Flyway ya aplicadas quedan registradas y no se repiten al reiniciar.

## 5. Seguridad, tablas y respaldos

- Roles separados: `CLIENTE` solo registra y ve sus solicitudes; `ADMIN` usa el menú exclusivo para editar servicios, consultar solicitudes y crear trabajadores. No hay registro público de administradores. La comprobación de permisos se realiza en Spring Security, no únicamente con enlaces React.
- Autenticación: contraseñas BCrypt, sesiones de 8 h con token aleatorio, solo hash SHA-256 del token almacenado en la tabla `sesiones`. Protege frente a XSS el frontend y no publiques tokens en los logs.
- Tablas: `usuarios`, `sesiones`, `servicios`, `solicitudes`. `flyway_schema_history` guarda el historial de V1 y V2. No uses `hibernate.ddl-auto=create` en producción: se mantiene `validate` y Flyway gestiona migraciones.
- Los volúmenes de PostgreSQL **persisten tras `docker compose down`**. Para respaldar en desarrollo, desde la raíz con Docker Compose iniciado:

```bash
docker compose exec -T postgres sh -c 'pg_dump -U "$POSTGRES_USER" -d "$POSTGRES_DB"' > servicestech_backup.sql
```

Guarda este archivo fuera de Git y en un lugar seguro. Para restaurar en otra base de datos vacía, hazlo siguiendo un procedimiento controlado de respaldo/restauración que evite chocar con las migraciones Flyway. En el VPS usa `docker compose -f compose.deploy.yml exec -T postgres ...` y conserva backups externos.

## 6. Diagnóstico rápido

- **React abre pero Axios falla:** prueba `http://localhost:8088/api/servicios`; si falla, `docker compose logs frontend backend`. Con Nginx proxy no necesitas definir CORS para este flujo. `VITE_API_URL` queda fijado a `/api` durante la compilación frontend.
- **Puerto 5432 en uso:** usa `POSTGRES_PORT=5433` en `.env`. Mantén `postgres:5432` dentro del backend Docker.
- **Flyway falla:** inspecciona `docker compose logs backend`; evita crear tablas manualmente y comprueba el historial de migraciones.
- **El admin no entra:** revisa `ADMIN_EMAIL` y `ADMIN_PASSWORD` usados **en el primer arranque**; el bootstrap nunca resetea una cuenta ya creada.
- **GHCR deniega descarga:** comprueba que el paquete exista, el namespace sea minúsculo y que el usuario del VPS tenga `docker login` con permiso `read:packages` si es privado.
- **El deploy no aparece:** confirma `DEPLOY_ENABLED=true`, secrets SSH y que el evento sea **push a main**, no Pull Request ni tag.
- **Pruebas fallan en el primer push:** revisa el job correspondiente en **Actions**; el build necesita acceso externo a Maven Central, Gradle y npm. Los scripts locales no sustituyen la ejecución real en GitHub.

---

## 7. Documentación original del negocio y código (también aplicable ejecutando sin Docker)

A continuación se conserva la documentación más detallada de pantallas, clases, validaciones, precios y endpoints del proyecto base. **Las instrucciones de instalación manual en esa documentación son una alternativa a Docker; no hace falta ejecutarlas si seguiste el apartado 2 anterior.** Las credenciales de administrador que aparezcan en ejemplos manuales deben coincidir con las usadas para inicializar la cuenta; el administrador solicitado para esta versión es `admin@admin.com`.

---

# ServicesTech — versión integrada sobre la estructura ORIGINAL

> Esta versión **parte directamente de `ServicesTech.zip` y conserva los nombres y directorios originales**. La entrega anterior reemplazaba las pantallas de la base; esta corrección lo evita. `ServicesTech_Registro_Catalogo.zip` también se analizó y se integraron sus objetivos funcionales, sin incorporar archivos que dependían de migraciones inexistentes.

## 1. Descripción del negocio y usuarios

**ServicesTech S.A.C.**, negocio de soporte técnico de Lima, Perú: diagnóstico, mantenimiento preventivo, reparación lógica, instalación de sistemas, redes y asistencia empresarial. El sitio tiene la misma portada, banner e identidad del proyecto original y añade un catálogo consultado a PostgreSQL con **12 precios referenciales en S/**. Se muestran precios orientativos, no se cobra en línea ni se garantiza un presupuesto hasta revisar cada solicitud.

**Cliente:** visita la portada → examina servicios → crea cuenta en `/signup` → inicia sesión por `Portal Cliente` → envía una solicitud → consulta su estado y tarifa referencial histórica desde `/cliente`.

**Trabajador/administrador:** inicia sesión por `Acceso Personal` → accede a `/admin` → gestiona catálogo, solicitudes y registro de nuevo personal. Las cuentas de trabajadores solo las crea un administrador autenticado (o una cuenta inicial por variables de entorno). No existe registro público de administradores.

### Tecnologías existentes y añadidas

- **Backend original:** Spring Boot, Java 21, Gradle con `build.gradle.kts` (Kotlin DSL), MapStruct, Lombok y controladores REST en `pe.fullstack.servicesbackend.controller`.
- **Ampliación backend:** PostgreSQL, Spring Data JPA, Flyway, Spring Security, BCrypt y sesiones con tokens opacos.
- **Frontend original:** Node.js, Vite, React 19, Tailwind CSS 4, `Home`, `Cabecera`, `Cuerpo`, `Piedepagina`, `Login`, `Signup`, `Catalogo`, `Servicios`.
- **Ampliación frontend:** React Router, Axios, validación de formularios, espacios de cliente/administración y protección de rutas.

> Aclaración: el backend **está programado en Java**; el fichero `build.gradle.kts` usa Kotlin como lenguaje de configuración de Gradle. No son controladores escritos en Kotlin.

## 2. Estructura conservada, clases y responsabilidades

```text
ServicesTech/
├── README.md                         # Este manual
├── CAMBIOS_RESPECTO_AL_ORIGINAL.md    # Comparación de archivos
├── docker-compose.yml                # PostgreSQL de desarrollo (nuevo)
├── ServicesBackend/                  # Nombre original
│   ├── build.gradle.kts              # ORIGINAL + dependencias nuevas
│   ├── gradlew / gradlew.bat / gradle/wrapper/
│   ├── HELP.md                        # Conservado
│   └── src/
│       ├── main/java/pe/fullstack/servicesbackend/
│       │   ├── ServicesbackendApplication.java
│       │   ├── controller/            # ORIGINAL; conservado y conectado
│       │   │   ├── UserController.java    # /api/admin/usuarios, sin passwords
│       │   │   ├── ServiceController.java # /api/servicios, PostgreSQL
│       │   │   ├── UserMapping.java       # Ejercicio MapStruct original
│       │   │   └── request/              # DTO originales conservados
│       │   ├── config/                # ORIGINAL: errores/validación REST
│       │   └── portal/                # NUEVO: funcionalidad de negocio
│       │       ├── modelo/            # Usuario, Servicio, Solicitud, Sesion
│       │       ├── repositorio/       # Repositorios JPA
│       │       ├── servicio/          # IdentidadServicio (registro y acceso)
│       │       ├── controlador/       # Acceso, Cliente y Admin REST
│       │       ├── config/           # Seguridad, CORS, errores, admin inicial
│       │       └── dto/              # Peticiones y Respuestas
│       ├── main/resources/
│       │   ├── application.properties # Conexión PostgreSQL
│       │   └── db/migration/          # V1 tablas, V2 catálogo Perú
│       └── test/                      # Tests originales + tests portal
└── ServicesFrontend/                 # Nombre original
    ├── public/img/chan.png           # Banner ORIGINAL conservado
    ├── public/assets/logoST.svg      # Logo ORIGINAL conservado
    ├── vite.config.js                # Vite y Tailwind original
    ├── index.html                    # Original
    ├── package.json                  # Original + Axios/Router
    ├── .env.example                  # Nueva configuración API
    └── src/
        ├── App.jsx                   # Conservado como entrada de las rutas
        ├── App.css / index.css
        ├── main.jsx                  # Montaje React ORIGINAL
        ├── componentes/
        │   ├── Cabecera.jsx          # Menús originales, ahora con Link
        │   ├── Cuerpo.jsx            # Portada original con botón funcional
        │   ├── Piedepagina.jsx       # Pie original conservado
        │   ├── BuscarUsuario.jsx     # Consulta admin sin contraseñas
        │   └── Restringida.jsx       # NUEVO: control de sesión/rol
        ├── pantallas/
        │   ├── Home.jsx              # Contenedor ORIGINAL + Outlet
        │   ├── Login.jsx             # Login original completo
        │   ├── Signup.jsx            # Registro original completo
        │   ├── Catalogo.jsx          # Ahora consulta catálogo con Axios
        │   ├── Servicios.jsx         # Ruta original del catálogo
        │   ├── PanelCliente.jsx      # NUEVO
        │   ├── PanelAdmin.jsx        # NUEVO
        │   └── NoEncontrado.jsx      # NUEVO
        ├── api/http.js               # NUEVO: Axios y token
        ├── contexto/Autenticacion.jsx# NUEVO: inicio/cierre de sesión
        └── util/validaciones.js      # NUEVO: validaciones cliente
```

`UserMapping`, `CreateUserRequest`, `UpdateUserRequest` y el DTO antiguo `User` se conservaron para no romper la organización/aprendizaje originales. El registro real utiliza ahora `Peticiones.Registro` + `IdentidadServicio` y la entidad JPA `portal.modelo.Usuario`. No se usan listas Java de ejemplo ni se envían contraseñas al navegador. La propiedad `password` del DTO heredado está marcada `@JsonIgnore`.

### Qué hacen las clases importantes

| Clase / componente | Función en el sistema |
|---|---|
| `Home.jsx` y `Cabecera.jsx` | Estructura general, encabezado, menús y enlaces con React Router. |
| `Cuerpo.jsx` | Portada original con banner de ServicesTech. |
| `Login.jsx` | Distingue `/login/cliente` y `/login/personal`; usa Axios y validaciones. |
| `Signup.jsx` | Formulario cliente con correo y contraseña; guarda mediante `/api/auth/registro`. |
| `Catalogo.jsx` | Busca y filtra servicios con precios S/ de PostgreSQL. |
| `PanelCliente.jsx` | Registra y muestra solicitudes del cliente autenticado. |
| `PanelAdmin.jsx` | Gestiona servicios, solicitudes, trabajadores y consulta usuarios. |
| `UserController.java` | Controlador original ahora protegido; consulta usuarios sin claves. |
| `ServiceController.java` | Controlador original ahora sirve el catálogo real. |
| `AccesoControlador.java` | Registro, login, perfil, cierre de sesión. |
| `ClienteControlador.java` | Solicitudes de atención del usuario autenticado. |
| `AdminControlador.java` | Gestión del catálogo, estados y cuentas de personal. |
| `IdentidadServicio.java` | Normaliza emails, registra usuarios, BCrypt, tokens y sesiones. |
| `Seguridad.java` | Permisos por `CLIENTE` y `ADMIN` del lado servidor. |
| `Usuario`, `Servicio`, `Solicitud`, `Sesion` | Entidades PostgreSQL relacionadas por JPA. |
| `V1__create_portal.sql` | Crea las cuatro tablas y los índices. |
| `V2__catalogo_referencial_peru.sql` | Inserta 12 servicios con tarifas orientativas. |

## 3. Instalación y ejecución

**Requisitos:** Java 21, Node.js 22 o posterior, npm, PostgreSQL 17 (o Docker), y acceso a internet para instalar dependencias la primera vez. La instalación usa el `gradlew` incluido en el backend: no se necesita Gradle global.

### Paso 1. Base de datos

Desde la carpeta raíz del proyecto:

```bash
# Desde la raíz, tras copiar .env.example a .env:
docker compose up -d postgres
```

El contenedor crea `servicestech`, usuario `servicestech`, puerto `5432`; la contraseña local de ejemplo es `solo_desarrollo_123`. Puede sobreescribirse con `DB_PASSWORD` **antes del primer inicio del volumen**. Si ya tienes PostgreSQL instalado, crea la base y el usuario manualmente y ajusta `DB_URL`, `DB_USER`, `DB_PASSWORD`.

### Paso 2. Backend Spring Boot

En macOS o Linux:

```bash
cd ServicesBackend
chmod +x gradlew
export DB_URL='jdbc:postgresql://localhost:5432/servicestech'
export DB_USER='servicestech'
export DB_PASSWORD='solo_desarrollo_123'
export ADMIN_EMAIL='admin@admin.com'
export ADMIN_PASSWORD='admin123456789'
./gradlew bootRun
```

En Windows PowerShell, define las variables con `$env:DB_PASSWORD="..."` (y las demás) y ejecuta `./gradlew.bat bootRun`.

Se inicia en **http://localhost:8080**. Flyway crea las tablas y precarga los 12 servicios (solo al crear una base de datos nueva). Al iniciar, `AdministradorInicial` crea el primer administrador si no existe ya una cuenta con ese correo. `ADMIN_PASSWORD` exige como mínimo 12 caracteres; **cambia los datos de ejemplo**. El archivo `.env.example` incluye únicamente las credenciales solicitadas para demostración local; no las uses en producción. Si una cuenta con ese correo ya existe, el sistema no la convierte en administrador ni le cambia la contraseña.

### Paso 3. Frontend React

En otra terminal:

```bash
cd ServicesFrontend
cp .env.example .env
npm install
npm run dev
```

Visita **http://localhost:5173** (o el puerto que Vite muestre). Axios usa `VITE_API_URL=/api` y Vite reenvía `/api` hacia Spring Boot en `localhost:8080`; no requiere CORS para el desarrollo local. **Si ya tenías `.env` con `http://localhost:8080/api`, cámbialo a `/api` y reinicia Vite.** Al instalar, npm genera el `package-lock.json` de esta versión. El ZIP no contiene `node_modules`, `.env` con secretos ni residuos de compilación.

> Nota: las dependencias se necesitan **descargar** al instalar. El entorno donde se preparó esta entrega no permitió acceder a npm ni al distribuidor de Gradle; por ese motivo no se puede afirmar que aquí se haya ejecutado una integración con un PostgreSQL real. Consulta la sección de pruebas.

## 4. Rutas React Router en App.jsx (sin reemplazar Home)

| Ruta | Acceso | Uso |
| --- | --- | --- |
| `/` | Público | Banner e inicio original (`Cuerpo.jsx`) |
| `/servicios` | Público | Acceso a servicios desde el menú original |
| `/catalogo` | Público | Catálogo con filtros, búsqueda y precios desde API |
| `/signup` y `/registro` | Público | Misma pantalla `Signup.jsx`: registro solo de clientes |
| `/acceso/cliente` | Público | Login del cliente |
| `/acceso/personal` | Público | Login de personal administrador |
| `/cliente` | CLIENTE | Registrar y consultar solicitudes propias |
| `/admin` | ADMIN | Catálogo, solicitudes, usuarios y alta de personal |
| `/nosotros`, `/casos`, `/blog`, `/contacto` | Público | Información y enlaces de la cabecera original (contenido introductorio) |
| `*` | Público | Página 404 |

`Restringida.jsx` comprueba el rol en la UI. **La autorización real se ejecuta de nuevo en Spring Security**; cambiar JavaScript no permite acceder a las APIs protegidas.

## 5. Endpoints de Spring Boot

| Método y ruta | Permiso | Descripción |
| --- | --- | --- |
| `GET /api/servicios` | Público | `ServiceController` original: lista servicios activos |
| `GET /api/admin/usuarios` | ADMIN | `UserController` original: lista perfiles sin contraseñas |
| `GET /api/admin/usuarios/{id}` | ADMIN | Lee perfil por ID; no expone contraseñas |
| `POST /api/auth/registro` | Público | Registro de cliente, devuelve token y perfil |
| `POST /api/auth/cliente` | Público | Login de cliente |
| `POST /api/auth/personal` | Público | Login del personal |
| `GET /api/auth/perfil` | Autenticado | Verifica sesión y obtiene usuario actual |
| `POST /api/auth/salir` | Autenticado | Revoca sesión/token activo |
| `GET /api/cliente/solicitudes` | CLIENTE | Consulta solicitudes **propias** |
| `POST /api/cliente/solicitudes` | CLIENTE | Crea una solicitud sin pagar |
| `GET /api/admin/servicios` | ADMIN | Todos los servicios (inactivos incluidos) |
| `POST /api/admin/servicios` | ADMIN | Crea servicio |
| `PUT /api/admin/servicios/{id}` | ADMIN | Edita, activa o desactiva |
| `DELETE /api/admin/servicios/{id}` | ADMIN | Baja lógica, conserva historial |
| `GET /api/admin/solicitudes` | ADMIN | Todas las solicitudes |
| `PATCH /api/admin/solicitudes/{id}/estado` | ADMIN | Estado `PENDIENTE`, `EN_PROCESO`, `ATENDIDA`, `CANCELADA` |
| `POST /api/admin/personal` | ADMIN | Alta de otro administrador; devuelve perfil (no su token) |

Todas las rutas de sesión usan `Authorization: Bearer <token>`. Se emiten tokens aleatorios de 256 bits y **solo se guarda su hash SHA-256** en la tabla `sesiones`, con expiración de 8 horas; los tokens pueden revocarse con logout. React los mantiene en `sessionStorage` durante la sesión de esa pestaña. No se utiliza JWT: se eligieron **tokens opacos persistidos y revocables**, suficientes para esta arquitectura de una base PostgreSQL.

### Ejemplo rápido de registro

```bash
curl -X POST http://localhost:8080/api/auth/registro \
  -H 'Content-Type: application/json' \
  -d '{"nombres":"Ana","apellidos":"Rojas","email":"ana@ejemplo.com","telefono":"987654321","clave":"UnaClaveSegura2026!"}'
```

La respuesta devuelve el perfil **sin contraseña** y un token temporal. Para solicitar servicio desde API:

```bash
curl -X POST http://localhost:8080/api/cliente/solicitudes \
  -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"servicioId":1,"detalle":"Laptop con sobrecalentamiento y apagados repentinos","direccion":"Lima, Perú"}'
```

### Validación y errores

- Registro: nombres/apellidos mínimo 2 caracteres, email válido y único (sin diferenciar mayúsculas), contraseña mínimo 10 caracteres, teléfono opcional peruano de 9 dígitos iniciado con 9.
- Inicio de sesión: correo y contraseña obligatorios. El portal de clientes rechaza cuentas administrativas y el portal de personal rechaza clientes.
- Catálogo: código único de 3–45 caracteres (`A–Z`, dígitos, guion), categoría permitida, precio `>= 0` y duración entre 15 y 10080 minutos.
- Solicitud: servicio activo y existente, detalle de 10 a 700 caracteres. El `clienteId` se determina a partir de la sesión validada, **nunca del formulario**.
- HTTP: `400` para validación, `401` sin sesión, `403` sin rol, `404` recurso inexistente, `409` duplicado. `Errores.java` devuelve `message` y errores de campo sin exponer trazas ni contraseñas.
- Axios presenta los errores del backend; todos los formularios envían solicitudes reales al servidor.

## 6. Tablas y clases principales

| Tabla PostgreSQL | Clase JPA | Responsabilidad |
| --- | --- | --- |
| `usuarios` | `portal.modelo.Usuario` | Datos básicos, hash BCrypt y `rol` (`CLIENTE` / `ADMIN`) |
| `sesiones` | `portal.modelo.Sesion` | Hash de token y caducidad, ligado a un usuario |
| `servicios` | `portal.modelo.Servicio` | Código, nombre, categoría, precio PEN, duración, activo |
| `solicitudes` | `portal.modelo.Solicitud` | Cliente, servicio, precio referencial histórico, detalle, estado |

**Relaciones:** `Usuario 1 ── N Sesion`; `Usuario 1 ── N Solicitud`; `Servicio 1 ── N Solicitud`. La columna `solicitudes.precio_referencial` se copia al generar la solicitud para conservar la referencia de precio aun cuando el administrador actualice el catálogo.

**Clases clave:** `IdentidadServicio` administra usuarios, verifica BCrypt y genera sesiones; `Seguridad` revisa el token y los roles antes de permitir peticiones; `AdministradorInicial` crea la primera cuenta autorizada mediante variables de ambiente; `ClienteControlador` crea y devuelve solicitudes propias; `AdminControlador` administra catálogo, solicitudes y personal; `ServiceController` original ofrece el catálogo público; `UserController` original permite consultar perfiles solamente al administrador; `AccesoControlador` expone los accesos y logout; `Peticiones` y `Respuestas` evitan exponer las entidades completas. `http.js` centraliza Axios; `Autenticacion.jsx` mantiene el perfil y `App.jsx` declara las rutas React Router. `V1` crea tablas/índices y `V2` carga catálogo con Flyway.

## 7. Catálogo y precios referenciales en Perú

Son estimaciones comerciales para Lima, **no promedios estadísticos** ni tarifas oficiales o garantizadas. Se eligieron puntos razonables dentro de rangos públicos publicados por prestadores de servicios al 16 de octubre de 2026. Montos en soles; pueden variar por distrito, complejidad, disponibilidad, traslado, IGV, repuestos y licencias.

| Servicio cargado en base de datos | Precio referencial |
| --- | ---: |
| Diagnóstico de PC o laptop | S/ 35 |
| Mantenimiento preventivo convencional | S/ 95 |
| Mantenimiento gaming | S/ 190 |
| Formateo e instalación del sistema | S/ 95 |
| Respaldo, Windows y configuración inicial | S/ 150 |
| Soporte remoto (sesión) | S/ 60 |
| Mano de obra de SSD y migración | S/ 120 |
| Recuperación inicial de datos | S/ 200 |
| Optimización Wi-Fi | S/ 95 |
| Configuración básica de red para oficina | S/ 250 |
| Instalación de cámara, solo mano de obra/unidad | S/ 80 |
| Visita de soporte para empresa | S/ 120 |

**Fuentes de comparación** (tarifarios de proveedores independientes, no afiliados con ServicesTech):

- [Soporte IT Perú](https://soporteitperu.com/servicios.html): mantenimiento S/60; formateo Windows S/80; SSD y migración S/120; redes desde S/250; datos desde S/200.
- [ServiTeam](https://serviteam.pe/): formateo desde S/60; mantenimiento regular S/120 y gaming S/180.
- [Marseth](https://marseth.pe/): mantenimiento convencional S/150, gaming desde S/190 y recuperación desde S/190.
- [Vgluu, servicios técnicos](https://vgluu.com/pe/servicio-tecnico): rangos de diagnóstico S/30–50, mantenimiento laptop S/60–90, soporte remoto S/50–80 y visita S/80–120.
- [HALPII, instalación de cámaras](https://portal.halpii.com/servicios/camaras-seguridad/lima): mano de obra habitual S/50–100 por cámara, según cableado y altura.

Para ajustar precios, **edita servicios desde el panel administrativo**. No alteres una migración Flyway aplicada: en instalaciones existentes, cambia el catálogo por API o agrega una migración `V3` si necesitas cambios masivos reproducibles.

## 8. Verificar que funciona

Con PostgreSQL y backend activos, prueba este recorrido:

1. Ingresa a `/catalogo`: deben mostrarse los 12 servicios y sus precios cargados por Flyway.
2. Visita `/signup` o `/registro`, crea una cuenta de cliente válida: se inserta en `usuarios`, contraseña BCrypt; el correo duplicado debe fallar.
3. Solicita un servicio desde `/cliente`: se crea fila en `solicitudes`, con estado `PENDIENTE` y referencia monetaria guardada.
4. Cierra sesión: el token queda revocado en `sesiones`.
5. Entra por `/acceso/personal` con las credenciales de entorno. En `/admin`, visualiza solicitudes, cambia estado y edita el precio de un servicio.
6. Verifica desde la sesión del cliente el nuevo estado: el importe histórico de la solicitud debe permanecer igual.
7. Comprueba que un cliente no pueda llamar `/api/admin/servicios`: debe devolver `403`; que un usuario sin token no pueda leer `/api/cliente/solicitudes`: `401`.
8. Crea otro trabajador en el panel, sal y comprueba su acceso desde `/acceso/personal`.

Pruebas disponibles:

```bash
cd ServicesFrontend
npm test              # Pruebas Node de validación (sin dependencias externas)
npm run lint
npm run build         # Requiere npm install previo

cd ../ServicesBackend
./gradlew test        # Pruebas unitarias + contextLoads (requiere PostgreSQL)
```

**Verificación realizada durante esta entrega:** pasaron las 5 pruebas de validación Node; 18 archivos JS/JSX fueron analizados por Babel y 34 Java pasaron análisis sintáctico sin importaciones locales JS faltantes. **No se pudo ejecutar** Gradle, descargar npm, compilar Vite ni realizar un ensayo E2E contra PostgreSQL debido a bloqueo de resolución de DNS para repositorios de paquetes en este entorno. Debe ejecutarse el recorrido anterior al desplegar.

## 9. Consideraciones de producción

- Usa variables de ambiente y secretos seguros; no pongas contraseñas en `.env` versionados. Usa TLS/HTTPS y ajusta `FRONTEND_ORIGINS` a los orígenes permitidos por CORS.
- Esta solución **no implementa** pago online, emisión de comprobantes, agenda con horarios, notificaciones, recuperación de contraseña ni emails transaccionales. Las solicitudes quedan registradas para gestión manual y seguimiento.
- Los tokens no son JWT, y la app no incluye limitación de intentos de acceso, auditoría administrativa ni limpieza programada de sesiones expiradas. Añade rate limiting, monitoreo, copias de seguridad y controles de privacidad antes de producción.
- `sessionStorage` limita la persistencia al navegador/pestaña pero es accesible por JavaScript: configura CSP, evita dependencias/scripts no confiables y evalúa cookies HttpOnly con CSRF si evolucionas la arquitectura.
- El backend usa `ddl-auto=validate`, por lo que **Flyway es el único mecanismo** que crea tablas. Respeta el orden de migraciones y guarda respaldos de PostgreSQL.

## Correccion Spring Boot 4 / Flyway

Si aparece `Schema validation: missing table [servicios]`, revisar `CORRECCION_FLYWAY.md`. Esta entrega incorpora `spring-boot-starter-flyway` para ejecutar las migraciones al arrancar.

## 10. Correccion de conexion Axios y CORS

- `ServicesFrontend/vite.config.js`: proxy de `/api` a Spring Boot (`127.0.0.1:8080`). Se preservan React y Tailwind originales.
- `ServicesFrontend/src/api/http.js`: Axios usa `/api` como URL base; mantiene Bearer token y ahora diferencia errores HTTP, timeouts y errores de red.
- `ServicesFrontend/.env.example`: usa `VITE_API_URL=/api`. Si creaste `.env` antes, cambia ese valor manualmente y reinicia `npm run dev`.
- `ServicesBackend/portal/config/Seguridad.java`: CORS admite una lista *explícita* de orígenes para clientes que contacten directamente al backend (sin proxy). Se ajusta con `FRONTEND_ORIGINS` en producción.

**Prueba de humo (dos terminales con Spring Boot y Vite en ejecución):**

```bash
curl -i http://localhost:8080/api/servicios
curl -i http://localhost:5173/api/servicios
```

Ambos deben devolver `200 OK` y el mismo JSON. Si Vite se ejecuta en otro puerto, sustitúyelo en la segunda URL. Si el backend responde pero el proxy no, comprueba que Vite fue reiniciado tras modificar `vite.config.js` y `.env`.

**Prueba de CORS sin proxy**, cuando se necesite acceder directamente:

```bash
curl -i -X OPTIONS http://localhost:8080/api/auth/registro \
  -H 'Origin: http://localhost:5173' \
  -H 'Access-Control-Request-Method: POST' \
  -H 'Access-Control-Request-Headers: content-type'
```

El resultado debe incluir `Access-Control-Allow-Origin: http://localhost:5173`.
