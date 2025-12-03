-- Script de inicialización para clients_db

-- Crear extensión para UUID si es necesaria
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla: cliente
CREATE TABLE IF NOT EXISTS cliente (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    telefono VARCHAR(20),
    direccion VARCHAR(255),
    keycloak_user_id VARCHAR(100) UNIQUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    activo BOOLEAN DEFAULT TRUE
);

-- Tabla: contenedor
CREATE TABLE IF NOT EXISTS contenedor (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    peso_kg DECIMAL(10, 2) NOT NULL,
    volumen_m3 DECIMAL(10, 2) NOT NULL,
    alto_m DECIMAL(5, 2),
    ancho_m DECIMAL(5, 2),
    largo_m DECIMAL(5, 2),
    descripcion TEXT,
    cliente_id BIGINT NOT NULL,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_contenedor_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id)
);

-- Tabla: ubicacion
CREATE TABLE IF NOT EXISTS ubicacion (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(20) NOT NULL,
    direccion TEXT NOT NULL,
    latitud DECIMAL(10, 8) NOT NULL,
    longitud DECIMAL(11, 8) NOT NULL,
    descripcion TEXT,
    deposito_id BIGINT,
    CONSTRAINT chk_ubicacion_tipo CHECK (tipo IN ('ORIGEN', 'DESTINO', 'DEPOSITO'))
);

-- Tabla: solicitud
CREATE TABLE IF NOT EXISTS solicitud (
    id BIGSERIAL PRIMARY KEY,
    numero_solicitud VARCHAR(50) UNIQUE NOT NULL,
    contenedor_id BIGINT NOT NULL,
    cliente_id BIGINT NOT NULL,
    ubicacion_origen_id BIGINT NOT NULL,
    ubicacion_destino_id BIGINT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    costo_estimado DECIMAL(12, 2),
    tiempo_estimado_horas INTEGER,
    costo_final DECIMAL(12, 2),
    tiempo_real_horas INTEGER,
    ruta_id BIGINT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_ultima_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    CONSTRAINT fk_solicitud_contenedor FOREIGN KEY (contenedor_id) REFERENCES contenedor(id),
    CONSTRAINT fk_solicitud_cliente FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CONSTRAINT fk_solicitud_origen FOREIGN KEY (ubicacion_origen_id) REFERENCES ubicacion(id),
    CONSTRAINT fk_solicitud_destino FOREIGN KEY (ubicacion_destino_id) REFERENCES ubicacion(id),
    CONSTRAINT chk_solicitud_estado CHECK (estado IN ('BORRADOR', 'PROGRAMADA', 'EN_TRANSITO', 'ENTREGADA', 'CANCELADA'))
);

-- Tabla: estado_solicitud
CREATE TABLE IF NOT EXISTS estado_solicitud (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    estado VARCHAR(30) NOT NULL,
    fecha_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observacion TEXT,
    usuario VARCHAR(100),
    CONSTRAINT fk_estado_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitud(id) ON DELETE CASCADE
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_contenedor_cliente ON contenedor(cliente_id);
CREATE INDEX IF NOT EXISTS idx_contenedor_codigo ON contenedor(codigo);
CREATE INDEX IF NOT EXISTS idx_ubicacion_coords ON ubicacion(latitud, longitud);
CREATE INDEX IF NOT EXISTS idx_solicitud_numero ON solicitud(numero_solicitud);
CREATE INDEX IF NOT EXISTS idx_solicitud_cliente ON solicitud(cliente_id);
CREATE INDEX IF NOT EXISTS idx_solicitud_estado ON solicitud(estado);
CREATE INDEX IF NOT EXISTS idx_solicitud_contenedor ON solicitud(contenedor_id);
CREATE INDEX IF NOT EXISTS idx_solicitud_fecha_creacion ON solicitud(fecha_creacion DESC);
CREATE INDEX IF NOT EXISTS idx_estado_solicitud_solicitud ON estado_solicitud(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_estado_solicitud_fecha ON estado_solicitud(fecha_hora);
CREATE INDEX IF NOT EXISTS idx_estado_solicitud_comp ON estado_solicitud(solicitud_id, fecha_hora DESC);

-- Datos de prueba (opcional)
-- INSERT INTO cliente (nombre, apellido, email, telefono, direccion) 
-- VALUES ('Juan', 'Pérez', 'juan.perez@example.com', '+5491112345678', 'Av. Corrientes 1234, CABA');

COMMENT ON TABLE cliente IS 'Almacena información de los clientes que solicitan transporte';
COMMENT ON TABLE contenedor IS 'Contenedores a transportar con sus dimensiones';
COMMENT ON TABLE ubicacion IS 'Ubicaciones geográficas (origen, destino, depósitos)';
COMMENT ON TABLE solicitud IS 'Solicitudes de transporte con estado y costos';
COMMENT ON TABLE estado_solicitud IS 'Historial de cambios de estado de las solicitudes';
