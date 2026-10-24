-- =============================================================================
-- V3 - Alinea el esquema con el ERD del Taller 1 (ServicesTech.sql)
--
-- V1 y V2 ya pueden estar aplicadas en Render, por eso NO se editan: esta
-- migración transforma lo existente sin perder datos y agrega las tablas del
-- flujo operativo:
--   solicitud -> orden (+ detalle) -> asignación de técnico -> historial
--   -> solución -> satisfacción
--
-- Nombres de tablas, columnas y catálogos iguales a los del ERD. Extensiones
-- documentadas en docs/MODELO_DATOS.md (clave_hash, sesiones, campos de catálogo
-- de servicios, id_servicio y dirección en solicitudes, horas_objetivo en
-- prioridad, precio_referencial en detalle_orden).
-- =============================================================================

-- 1. Convención de nombres del ERD en las tablas de V1 -------------------------
ALTER TABLE usuarios RENAME COLUMN id TO id_usuario;
ALTER TABLE usuarios RENAME COLUMN creado_en TO fecha_registro;
ALTER TABLE usuarios ADD COLUMN dni VARCHAR(8);
ALTER TABLE usuarios ADD CONSTRAINT usuarios_dni_formato CHECK (dni IS NULL OR dni ~ '^[0-9]{8}$');
CREATE UNIQUE INDEX usuarios_dni_unico ON usuarios (dni);
-- V1 permitía teléfono vacío (''): se normaliza a NULL antes de exigir el formato.
UPDATE usuarios SET telefono = NULL WHERE telefono IS NOT NULL AND btrim(telefono) = '';
ALTER TABLE usuarios ADD CONSTRAINT usuarios_telefono_formato CHECK (telefono IS NULL OR telefono ~ '^9[0-9]{8}$');

ALTER TABLE sesiones RENAME COLUMN id TO id_sesion;
ALTER TABLE sesiones RENAME COLUMN usuario_id TO id_usuario;
CREATE INDEX sesiones_usuario_idx ON sesiones (id_usuario);

ALTER TABLE servicios RENAME COLUMN id TO id_servicio;

-- 2. Roles (relación N:M usuario_rol, como en el ERD) --------------------------
CREATE TABLE roles (
 id_rol BIGSERIAL PRIMARY KEY,
 nombre VARCHAR(30) NOT NULL UNIQUE,
 descripcion VARCHAR(150)
);
INSERT INTO roles (nombre, descripcion) VALUES
 ('CLIENTE', 'Usuario que registra solicitudes y consulta sus servicios'),
 ('TECNICO', 'Usuario encargado de atender las ordenes del servicio'),
 ('ADMINISTRADOR', 'Usuario encargado de administrar y supervisar el sistema');

CREATE TABLE usuario_rol (
 id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
 id_rol BIGINT NOT NULL REFERENCES roles (id_rol) ON UPDATE CASCADE ON DELETE RESTRICT,
 fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 PRIMARY KEY (id_usuario, id_rol)
);
INSERT INTO usuario_rol (id_usuario, id_rol)
SELECT u.id_usuario, r.id_rol
FROM usuarios u
JOIN roles r ON r.nombre = CASE u.rol WHEN 'ADMIN' THEN 'ADMINISTRADOR' ELSE u.rol END;
ALTER TABLE usuarios DROP COLUMN rol;

-- 3. Catálogos de prioridad y estados -----------------------------------------
CREATE TABLE prioridad (
 id_prioridad BIGSERIAL PRIMARY KEY,
 nombre VARCHAR(30) NOT NULL UNIQUE,
 descripcion VARCHAR(150),
 horas_objetivo INTEGER NOT NULL CHECK (horas_objetivo > 0)
);
INSERT INTO prioridad (nombre, descripcion, horas_objetivo) VALUES
 ('BAJA', 'Atención no urgente', 120),
 ('MEDIA', 'Atención normal', 72),
 ('ALTA', 'Requiere atención prioritaria', 48),
 ('CRITICA', 'Requiere atención inmediata', 24);

CREATE TABLE estado_solicitud (
 id_estado_solicitud BIGSERIAL PRIMARY KEY,
 nombre VARCHAR(30) NOT NULL UNIQUE,
 descripcion VARCHAR(150)
);
INSERT INTO estado_solicitud (nombre, descripcion) VALUES
 ('PENDIENTE', 'Solicitud recién registrada'),
 ('EN_REVISION', 'Solicitud siendo evaluada'),
 ('APROBADA', 'Solicitud aprobada'),
 ('RECHAZADA', 'Solicitud rechazada'),
 ('CANCELADA', 'Solicitud cancelada');

CREATE TABLE estados_orden (
 id_estado BIGSERIAL PRIMARY KEY,
 nombre VARCHAR(30) NOT NULL UNIQUE,
 descripcion VARCHAR(150)
);
INSERT INTO estados_orden (nombre, descripcion) VALUES
 ('PENDIENTE', 'Orden pendiente de asignación'),
 ('ASIGNADA', 'Orden con técnico asignado'),
 ('EN_ATENCION', 'Orden actualmente en atención'),
 ('RESUELTA', 'Problema solucionado'),
 ('CERRADA', 'Orden finalizada'),
 ('CANCELADA', 'Orden cancelada');

-- 4. Solicitudes: columnas del ERD (asunto, prioridad, estado como FK) ---------
ALTER TABLE solicitudes RENAME COLUMN id TO id_solicitud;
ALTER TABLE solicitudes RENAME COLUMN cliente_id TO id_usuario;
ALTER TABLE solicitudes RENAME COLUMN servicio_id TO id_servicio;
ALTER TABLE solicitudes RENAME COLUMN detalle TO descripcion;
ALTER TABLE solicitudes RENAME COLUMN creado_en TO fecha_solicitud;
ALTER INDEX solicitudes_cliente_idx RENAME TO solicitudes_usuario_idx;

ALTER TABLE solicitudes ADD COLUMN asunto VARCHAR(150);
UPDATE solicitudes s SET asunto = sv.nombre FROM servicios sv WHERE sv.id_servicio = s.id_servicio;
ALTER TABLE solicitudes ALTER COLUMN asunto SET NOT NULL;

ALTER TABLE solicitudes ADD COLUMN id_prioridad BIGINT REFERENCES prioridad (id_prioridad) ON UPDATE CASCADE ON DELETE RESTRICT;
UPDATE solicitudes SET id_prioridad = (SELECT id_prioridad FROM prioridad WHERE nombre = 'MEDIA');
ALTER TABLE solicitudes ALTER COLUMN id_prioridad SET NOT NULL;

-- Estados de V1 -> catálogo del ERD:
-- PENDIENTE -> PENDIENTE, EN_PROCESO -> EN_REVISION, ATENDIDA -> APROBADA, CANCELADA -> CANCELADA
ALTER TABLE solicitudes ADD COLUMN id_estado_solicitud BIGINT REFERENCES estado_solicitud (id_estado_solicitud) ON UPDATE CASCADE ON DELETE RESTRICT;
UPDATE solicitudes s SET id_estado_solicitud = e.id_estado_solicitud
FROM estado_solicitud e
WHERE e.nombre = CASE s.estado
 WHEN 'EN_PROCESO' THEN 'EN_REVISION'
 WHEN 'ATENDIDA' THEN 'APROBADA'
 ELSE s.estado END;
ALTER TABLE solicitudes ALTER COLUMN id_estado_solicitud SET NOT NULL;
ALTER TABLE solicitudes DROP COLUMN estado;

CREATE INDEX solicitudes_estado_idx ON solicitudes (id_estado_solicitud);
CREATE INDEX solicitudes_servicio_idx ON solicitudes (id_servicio);

-- 5. Órdenes y su detalle ------------------------------------------------------
CREATE TABLE ordenes (
 id_orden BIGSERIAL PRIMARY KEY,
 fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 fecha_limite TIMESTAMPTZ NOT NULL,
 fecha_cierre TIMESTAMPTZ,
 observaciones VARCHAR(200),
 id_solicitud BIGINT NOT NULL UNIQUE REFERENCES solicitudes (id_solicitud) ON UPDATE CASCADE ON DELETE RESTRICT,
 id_estado BIGINT NOT NULL REFERENCES estados_orden (id_estado) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT ordenes_fecha_limite CHECK (fecha_limite >= fecha_creacion),
 CONSTRAINT ordenes_fecha_cierre CHECK (fecha_cierre IS NULL OR fecha_cierre >= fecha_creacion)
);
CREATE INDEX ordenes_estado_idx ON ordenes (id_estado);

CREATE TABLE detalle_orden (
 id_detalle_orden BIGSERIAL PRIMARY KEY,
 id_orden BIGINT NOT NULL REFERENCES ordenes (id_orden) ON UPDATE CASCADE ON DELETE RESTRICT,
 id_servicio BIGINT NOT NULL REFERENCES servicios (id_servicio) ON UPDATE CASCADE ON DELETE RESTRICT,
 precio_referencial NUMERIC(10,2) NOT NULL CHECK (precio_referencial >= 0),
 CONSTRAINT detalle_orden_unico UNIQUE (id_orden, id_servicio)
);

-- 6. Asignación de técnicos e historial de estados -----------------------------
CREATE TABLE asignaciones_tecnico (
 id_asignacion BIGSERIAL PRIMARY KEY,
 fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 fecha_fin TIMESTAMPTZ,
 id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
 id_orden BIGINT NOT NULL REFERENCES ordenes (id_orden) ON UPDATE CASCADE ON DELETE RESTRICT,
 CONSTRAINT asignaciones_fecha_fin CHECK (fecha_fin IS NULL OR fecha_fin >= fecha_asignacion)
);
-- Una orden tiene como máximo un técnico con asignación vigente.
CREATE UNIQUE INDEX asignaciones_vigente_unica ON asignaciones_tecnico (id_orden) WHERE fecha_fin IS NULL;
CREATE INDEX asignaciones_usuario_idx ON asignaciones_tecnico (id_usuario);

CREATE TABLE historial_estado_orden (
 id_historial BIGSERIAL PRIMARY KEY,
 fecha_cambio TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 observacion VARCHAR(200),
 id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
 id_orden BIGINT NOT NULL REFERENCES ordenes (id_orden) ON UPDATE CASCADE ON DELETE RESTRICT,
 id_estado BIGINT NOT NULL REFERENCES estados_orden (id_estado) ON UPDATE CASCADE ON DELETE RESTRICT
);
CREATE INDEX historial_orden_idx ON historial_estado_orden (id_orden, fecha_cambio);

-- 7. Solución y satisfacción (una por orden) -----------------------------------
CREATE TABLE solucion_servicio (
 id_solucion BIGSERIAL PRIMARY KEY,
 descripcion_solucion VARCHAR(500) NOT NULL,
 fecha_solucion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 id_usuario BIGINT NOT NULL REFERENCES usuarios (id_usuario) ON UPDATE CASCADE ON DELETE RESTRICT,
 id_orden BIGINT NOT NULL UNIQUE REFERENCES ordenes (id_orden) ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE TABLE satisfaccion (
 id_satisfaccion BIGSERIAL PRIMARY KEY,
 calificacion SMALLINT NOT NULL CHECK (calificacion >= 1 AND calificacion <= 5),
 comentario VARCHAR(200),
 fecha_evaluacion TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 id_orden BIGINT NOT NULL UNIQUE REFERENCES ordenes (id_orden) ON UPDATE CASCADE ON DELETE RESTRICT
);
