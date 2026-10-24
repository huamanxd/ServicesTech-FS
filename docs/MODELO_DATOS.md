# Modelo de datos

El esquema de la aplicación es el **ERD del Taller 1** (`Taller1/ServicesTech.sql`, diagrama `ServiceTech_lucid.png`) con los mismos nombres de tablas, columnas y catálogos. Lo crean las migraciones Flyway de `ServicesBackend/src/main/resources/db/migration`:

| Migración | Contenido |
|---|---|
| `V1__create_portal.sql` | Primera versión: usuarios, sesiones, servicios, solicitudes. |
| `V2__catalogo_referencial_peru.sql` | 12 servicios con precios referenciales en soles. |
| `V3__modelo_erd_taller1.sql` | Convierte V1 al ERD sin perder datos y agrega el flujo operativo. |

> V1 y V2 ya pueden estar aplicadas en Render. **Nunca se editan**: cualquier cambio va en una migración nueva (V4, V5…).

Como prueba de consistencia, las 11 consultas de `Taller1/consultasNegocio.sql` se ejecutan sin cambios sobre esta base.

## Tablas

| Tabla (ERD) | Entidad JPA | Qué guarda |
|---|---|---|
| `usuarios` | `Usuario` | Datos personales, DNI, hash BCrypt de la clave, `activo`. Los usuarios no se eliminan. |
| `roles` | `Rol` | `CLIENTE` (1), `TECNICO` (2), `ADMINISTRADOR` (3). |
| `usuario_rol` | relación N:M en `Usuario.roles` | Roles de cada usuario y su fecha de asignación. |
| `prioridad` | `Prioridad` | `BAJA`, `MEDIA`, `ALTA`, `CRITICA` + horas objetivo de atención. |
| `estado_solicitud` | `EstadoSolicitud` | `PENDIENTE`, `EN_REVISION`, `APROBADA`, `RECHAZADA`, `CANCELADA`. |
| `solicitudes` | `Solicitud` | Asunto, descripción, prioridad, estado, cliente y servicio solicitado. |
| `estados_orden` | `EstadoOrden` | `PENDIENTE`, `ASIGNADA`, `EN_ATENCION`, `RESUELTA`, `CERRADA`, `CANCELADA`. |
| `ordenes` | `Orden` | Una por solicitud aprobada: fechas de creación, límite y cierre, estado. |
| `detalle_orden` | `DetalleOrden` | Servicios incluidos en la orden y su precio referencial. |
| `asignaciones_tecnico` | `AsignacionTecnico` | Historial de técnicos asignados; vigente si `fecha_fin` es NULL (índice único parcial: una vigente por orden). |
| `historial_estado_orden` | `HistorialEstadoOrden` | Bitácora: cada cambio de estado con usuario, fecha y observación. |
| `servicios` | `Servicio` | Catálogo. Baja lógica con `activo = false`. |
| `solucion_servicio` | `SolucionServicio` | Solución registrada por el técnico (una por orden). |
| `satisfaccion` | `Satisfaccion` | Calificación 1–5 del cliente y comentario (una por orden). |
| `sesiones` | `Sesion` | Hash SHA-256 de cada JWT emitido y su vencimiento (permite cerrar sesión y revocar accesos). |

## Diferencias con `ServicesTech.sql` (y por qué)

| Diferencia | Motivo |
|---|---|
| Claves `BIGSERIAL` y fechas `TIMESTAMPTZ` | Vienen de V1 (ya desplegada) y evitan ambigüedades de zona horaria. |
| `usuarios.clave` → `clave_hash` | Se guarda solo el hash BCrypt; el nombre lo deja explícito. |
| `usuarios.dni` y `telefono` admiten NULL en la base | Las cuentas creadas antes de V3 no tenían DNI. La API los exige en todo registro nuevo. |
| Email único sin distinguir mayúsculas | El ERD no lo restringía; dos cuentas con el mismo correo rompían el inicio de sesión. |
| `servicios` con `codigo`, `categoria`, `precio_referencial`, `duracion_minutos` | Catálogo público con precios en soles. |
| `solicitudes.id_servicio`, `direccion`, `precio_referencial` | El cliente elige el servicio al pedirlo y se conserva el precio que vio. |
| `prioridad.horas_objetivo` | Calcula la fecha límite sugerida de la orden (CRITICA 24 h, ALTA 48 h, MEDIA 72 h, BAJA 120 h). |
| `detalle_orden.precio_referencial` | Total referencial de la orden aunque luego cambie el catálogo. |
| Tabla `sesiones` | Revocación de tokens (logout, cuenta desactivada). |

## Migración de datos existentes (V3)

- `usuarios.rol` pasa a `usuario_rol`: `ADMIN` → `ADMINISTRADOR`, `CLIENTE` → `CLIENTE`.
- Estados de solicitud: `PENDIENTE` → `PENDIENTE`, `EN_PROCESO` → `EN_REVISION`, `ATENDIDA` → `APROBADA`, `CANCELADA` → `CANCELADA`.
- Solicitudes antiguas: `asunto` = nombre del servicio, prioridad `MEDIA`.
- Teléfonos vacíos (`''`) pasan a NULL.

Se probó sobre una base con datos creados por la versión anterior: los usuarios siguen entrando con su contraseña y ven sus solicitudes.

## Reglas de estado

| Entidad | Desde | Puede pasar a | Quién |
|---|---|---|---|
| Solicitud | `PENDIENTE` | `EN_REVISION`, `APROBADA`, `RECHAZADA` | Administrador |
| Solicitud | `EN_REVISION` | `APROBADA`, `RECHAZADA` | Administrador |
| Solicitud | `PENDIENTE`, `EN_REVISION`, `APROBADA` | `CANCELADA` (solo si aún no tiene orden) | Cliente |
| Solicitud | `APROBADA` | se genera la orden | Administrador |
| Orden | `PENDIENTE` | `ASIGNADA` | Administrador (asigna) o técnico (toma de la bandeja) |
| Orden | `ASIGNADA` | `EN_ATENCION` | Técnico |
| Orden | `ASIGNADA`, `EN_ATENCION` | `ASIGNADA` (reasignación) | Administrador |
| Orden | `EN_ATENCION` | `RESUELTA` (registra la solución) | Técnico |
| Orden | `RESUELTA` | `CERRADA` | Administrador, o automáticamente cuando el cliente califica |
| Orden | `PENDIENTE`, `ASIGNADA`, `EN_ATENCION` | `CANCELADA` (motivo obligatorio) | Administrador |

Están en `ReglasEstado.java`. Cualquier otra transición responde **409**. Cada cambio queda en `historial_estado_orden`.

## Indicadores de los dashboards

| Indicador | Definición |
|---|---|
| Orden abierta | Estado distinto de `CERRADA` y `CANCELADA` (equivale a `fecha_cierre IS NULL`). |
| Tiempo de atención | Desde `ordenes.fecha_creacion` hasta `solucion_servicio.fecha_solucion`. |
| Cumplimiento | % de órdenes resueltas en o antes de `fecha_limite`. |
| Retrasada | Resuelta después de la fecha límite, o sin resolver con la fecha límite vencida. Las canceladas no cuentan. |
| Satisfacción | Promedio de `satisfaccion.calificacion`. |
| Rendimiento técnico | Por técnico: órdenes atribuidas (las que resolvió o tiene vigentes), en curso, resueltas, retrasadas, tiempo promedio, cumplimiento y satisfacción. |

Se usa la fecha de solución, y no la de cierre, para no penalizar al técnico por la demora del cierre administrativo o de la calificación del cliente.
