-- Script de inicialización para logistics_db

-- Tabla: deposito
CREATE TABLE IF NOT EXISTS deposito (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(50) UNIQUE NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    direccion TEXT NOT NULL,
    latitud DECIMAL(10, 8) NOT NULL,
    longitud DECIMAL(11, 8) NOT NULL,
    capacidad_maxima_contenedores INTEGER DEFAULT 100,
    contenedores_actuales INTEGER DEFAULT 0,
    activo BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: transportista
CREATE TABLE IF NOT EXISTS transportista (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni VARCHAR(20) UNIQUE NOT NULL,
    telefono VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    licencia_conducir VARCHAR(50) NOT NULL,
    keycloak_user_id VARCHAR(100) UNIQUE,
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: camion
CREATE TABLE IF NOT EXISTS camion (
    id BIGSERIAL PRIMARY KEY,
    dominio VARCHAR(20) UNIQUE NOT NULL,
    marca VARCHAR(50),
    modelo VARCHAR(50),
    anio INTEGER,
    capacidad_peso_kg DECIMAL(10, 2) NOT NULL,
    capacidad_volumen_m3 DECIMAL(10, 2) NOT NULL,
    consumo_combustible_km_litro DECIMAL(5, 2) NOT NULL,
    costo_base_por_km DECIMAL(8, 2) NOT NULL,
    transportista_id BIGINT,
    estado VARCHAR(20) DEFAULT 'DISPONIBLE',
    activo BOOLEAN DEFAULT TRUE,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_camion_transportista FOREIGN KEY (transportista_id) REFERENCES transportista(id),
    CONSTRAINT chk_camion_estado CHECK (estado IN ('DISPONIBLE', 'OCUPADO', 'MANTENIMIENTO', 'INACTIVO'))
);

-- Tabla: ruta
CREATE TABLE IF NOT EXISTS ruta (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    tipo VARCHAR(20) DEFAULT 'PROPUESTA',
    cantidad_tramos INTEGER NOT NULL,
    cantidad_depositos INTEGER DEFAULT 0,
    distancia_total_km DECIMAL(10, 2),
    tiempo_estimado_total_horas INTEGER,
    costo_estimado_total DECIMAL(12, 2),
    costo_real_total DECIMAL(12, 2),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    seleccionada BOOLEAN DEFAULT FALSE,
    CONSTRAINT chk_ruta_tipo CHECK (tipo IN ('PROPUESTA', 'ASIGNADA'))
);

-- Tabla: tramo
CREATE TABLE IF NOT EXISTS tramo (
    id BIGSERIAL PRIMARY KEY,
    ruta_id BIGINT NOT NULL,
    numero_orden INTEGER NOT NULL,
    tipo_tramo VARCHAR(30) NOT NULL,
    
    -- Origen del tramo
    origen_tipo VARCHAR(20) NOT NULL,
    origen_direccion TEXT NOT NULL,
    origen_latitud DECIMAL(10, 8) NOT NULL,
    origen_longitud DECIMAL(11, 8) NOT NULL,
    origen_deposito_id BIGINT,
    
    -- Destino del tramo
    destino_tipo VARCHAR(20) NOT NULL,
    destino_direccion TEXT NOT NULL,
    destino_latitud DECIMAL(10, 8) NOT NULL,
    destino_longitud DECIMAL(11, 8) NOT NULL,
    destino_deposito_id BIGINT,
    
    -- Datos del tramo
    distancia_km DECIMAL(10, 2) NOT NULL,
    estado VARCHAR(20) DEFAULT 'ESTIMADO',
    camion_id BIGINT,
    
    -- Tiempos
    fecha_hora_inicio_estimada TIMESTAMP,
    fecha_hora_fin_estimada TIMESTAMP,
    fecha_hora_inicio_real TIMESTAMP,
    fecha_hora_fin_real TIMESTAMP,
    
    -- Costos
    costo_estimado DECIMAL(10, 2),
    costo_real DECIMAL(10, 2),
    
    -- Observaciones
    observaciones TEXT,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_tramo_ruta FOREIGN KEY (ruta_id) REFERENCES ruta(id) ON DELETE CASCADE,
    CONSTRAINT fk_tramo_camion FOREIGN KEY (camion_id) REFERENCES camion(id),
    CONSTRAINT fk_tramo_origen_deposito FOREIGN KEY (origen_deposito_id) REFERENCES deposito(id),
    CONSTRAINT fk_tramo_destino_deposito FOREIGN KEY (destino_deposito_id) REFERENCES deposito(id),
    CONSTRAINT chk_tramo_tipo CHECK (tipo_tramo IN ('ORIGEN_DEPOSITO', 'DEPOSITO_DEPOSITO', 'DEPOSITO_DESTINO', 'ORIGEN_DESTINO')),
    CONSTRAINT chk_tramo_estado CHECK (estado IN ('ESTIMADO', 'ASIGNADO', 'INICIADO', 'FINALIZADO'))
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_deposito_codigo ON deposito(codigo);
CREATE INDEX IF NOT EXISTS idx_deposito_coords ON deposito(latitud, longitud);
CREATE INDEX IF NOT EXISTS idx_transportista_dni ON transportista(dni);
CREATE INDEX IF NOT EXISTS idx_camion_dominio ON camion(dominio);
CREATE INDEX IF NOT EXISTS idx_camion_estado ON camion(estado);
CREATE INDEX IF NOT EXISTS idx_camion_transportista ON camion(transportista_id);
CREATE INDEX IF NOT EXISTS idx_camion_capacidad ON camion(capacidad_peso_kg, capacidad_volumen_m3);
CREATE INDEX IF NOT EXISTS idx_ruta_solicitud ON ruta(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_ruta_tipo ON ruta(tipo);
CREATE INDEX IF NOT EXISTS idx_tramo_ruta ON tramo(ruta_id);
CREATE INDEX IF NOT EXISTS idx_tramo_camion ON tramo(camion_id);
CREATE INDEX IF NOT EXISTS idx_tramo_estado ON tramo(estado);
CREATE INDEX IF NOT EXISTS idx_tramo_orden ON tramo(ruta_id, numero_orden);
CREATE INDEX IF NOT EXISTS idx_tramo_fechas ON tramo(fecha_hora_inicio_real, fecha_hora_fin_real);

-- Datos de prueba
-- Depósitos de ejemplo
INSERT INTO deposito (codigo, nombre, direccion, latitud, longitud, capacidad_maxima_contenedores) 
VALUES 
    ('DEP-001', 'Depósito CABA Norte', 'Av. Gral Paz 1234, CABA', -34.5869, -58.4398, 150),
    ('DEP-002', 'Depósito Zona Oeste', 'Ruta 3 Km 25, La Matanza', -34.6892, -58.5897, 200),
    ('DEP-003', 'Depósito Pilar', 'Panamericana Km 50, Pilar', -34.4587, -58.9142, 100)
ON CONFLICT (codigo) DO NOTHING;

-- Transportistas de ejemplo
INSERT INTO transportista (nombre, apellido, dni, telefono, licencia_conducir) 
VALUES 
    ('Carlos', 'González', '12345678', '+5491123456789', 'LIC-001-2025'),
    ('María', 'Rodríguez', '23456789', '+5491134567890', 'LIC-002-2025'),
    ('Pedro', 'Martínez', '34567890', '+5491145678901', 'LIC-003-2025')
ON CONFLICT (dni) DO NOTHING;

-- Camiones de ejemplo
INSERT INTO camion (dominio, marca, modelo, anio, capacidad_peso_kg, capacidad_volumen_m3, 
                    consumo_combustible_km_litro, costo_base_por_km, transportista_id, estado) 
VALUES 
    ('AA123BB', 'Mercedes-Benz', 'Actros 2651', 2020, 15000.00, 40.00, 0.35, 150.00, 1, 'DISPONIBLE'),
    ('CC456DD', 'Scania', 'R450', 2021, 18000.00, 50.00, 0.40, 180.00, 2, 'DISPONIBLE'),
    ('EE789FF', 'Volvo', 'FH16', 2019, 20000.00, 55.00, 0.45, 200.00, 3, 'DISPONIBLE')
ON CONFLICT (dominio) DO NOTHING;

COMMENT ON TABLE deposito IS 'Depósitos intermedios para almacenamiento temporal de contenedores';
COMMENT ON TABLE transportista IS 'Conductores de los camiones';
COMMENT ON TABLE camion IS 'Flota de camiones con capacidades y costos';
COMMENT ON TABLE ruta IS 'Rutas de transporte con múltiples tramos';
COMMENT ON TABLE tramo IS 'Segmentos individuales de una ruta entre ubicaciones';
