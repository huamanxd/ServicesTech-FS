CREATE TABLE usuarios (
 id BIGSERIAL PRIMARY KEY,
 nombres VARCHAR(90) NOT NULL,
 apellidos VARCHAR(90) NOT NULL,
 email VARCHAR(180) NOT NULL,
 telefono VARCHAR(15),
 clave_hash VARCHAR(100) NOT NULL,
 rol VARCHAR(15) NOT NULL CHECK (rol IN ('CLIENTE', 'ADMIN')),
 activo BOOLEAN NOT NULL DEFAULT TRUE,
 creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE UNIQUE INDEX usuarios_email_unico ON usuarios (lower(btrim(email)));
CREATE TABLE sesiones (
 id BIGSERIAL PRIMARY KEY,
 usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
 token_hash VARCHAR(64) NOT NULL UNIQUE,
 expira_en TIMESTAMPTZ NOT NULL
);
CREATE INDEX sesiones_expira_idx ON sesiones (expira_en);
CREATE TABLE servicios (
 id BIGSERIAL PRIMARY KEY,
 codigo VARCHAR(45) NOT NULL UNIQUE,
 nombre VARCHAR(120) NOT NULL,
 descripcion VARCHAR(600) NOT NULL,
 categoria VARCHAR(25) NOT NULL,
 precio_referencial NUMERIC(10,2) NOT NULL CHECK (precio_referencial >= 0),
 duracion_minutos INTEGER NOT NULL CHECK (duracion_minutos > 0),
 activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE solicitudes (
 id BIGSERIAL PRIMARY KEY,
 cliente_id BIGINT NOT NULL REFERENCES usuarios(id),
 servicio_id BIGINT NOT NULL REFERENCES servicios(id),
 detalle VARCHAR(700) NOT NULL,
 direccion VARCHAR(220),
 precio_referencial NUMERIC(10,2) NOT NULL,
 estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE','EN_PROCESO','ATENDIDA','CANCELADA')),
 creado_en TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX solicitudes_cliente_idx ON solicitudes (cliente_id, creado_en DESC);
