-- Script de inicialización para billing_db

-- Tabla: tarifa_base
CREATE TABLE IF NOT EXISTS tarifa_base (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    costo_fijo_gestion DECIMAL(10, 2) NOT NULL,
    costo_adicional_por_tramo DECIMAL(10, 2) DEFAULT 0,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: tarifa_combustible
CREATE TABLE IF NOT EXISTS tarifa_combustible (
    id BIGSERIAL PRIMARY KEY,
    precio_por_litro DECIMAL(8, 2) NOT NULL,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: tarifa_estadia
CREATE TABLE IF NOT EXISTS tarifa_estadia (
    id BIGSERIAL PRIMARY KEY,
    costo_por_dia DECIMAL(10, 2) NOT NULL,
    costo_por_hora DECIMAL(8, 2) NOT NULL,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: tarifa_peso_volumen
CREATE TABLE IF NOT EXISTS tarifa_peso_volumen (
    id BIGSERIAL PRIMARY KEY,
    peso_minimo_kg DECIMAL(10, 2) NOT NULL,
    peso_maximo_kg DECIMAL(10, 2) NOT NULL,
    volumen_minimo_m3 DECIMAL(10, 2) NOT NULL,
    volumen_maximo_m3 DECIMAL(10, 2) NOT NULL,
    multiplicador_costo DECIMAL(5, 2) DEFAULT 1.0,
    fecha_vigencia_desde DATE NOT NULL,
    fecha_vigencia_hasta DATE,
    activa BOOLEAN DEFAULT TRUE,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: costo_solicitud
CREATE TABLE IF NOT EXISTS costo_solicitud (
    id BIGSERIAL PRIMARY KEY,
    solicitud_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    
    -- Costos desglosados
    costo_gestion DECIMAL(10, 2) DEFAULT 0,
    costo_transporte DECIMAL(10, 2) DEFAULT 0,
    costo_combustible DECIMAL(10, 2) DEFAULT 0,
    costo_estadia DECIMAL(10, 2) DEFAULT 0,
    costo_adicionales DECIMAL(10, 2) DEFAULT 0,
    
    -- Total
    costo_total DECIMAL(12, 2) NOT NULL,
    
    -- Referencias a tarifas usadas
    tarifa_base_id BIGINT,
    tarifa_combustible_id BIGINT,
    tarifa_estadia_id BIGINT,
    
    fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    observaciones TEXT,
    
    CONSTRAINT fk_costo_tarifa_base FOREIGN KEY (tarifa_base_id) REFERENCES tarifa_base(id),
    CONSTRAINT fk_costo_tarifa_combustible FOREIGN KEY (tarifa_combustible_id) REFERENCES tarifa_combustible(id),
    CONSTRAINT fk_costo_tarifa_estadia FOREIGN KEY (tarifa_estadia_id) REFERENCES tarifa_estadia(id),
    CONSTRAINT chk_costo_tipo CHECK (tipo IN ('ESTIMADO', 'FINAL'))
);

-- Tabla: costo_tramo
CREATE TABLE IF NOT EXISTS costo_tramo (
    id BIGSERIAL PRIMARY KEY,
    tramo_id BIGINT NOT NULL,
    costo_solicitud_id BIGINT NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    
    -- Costos del tramo
    distancia_km DECIMAL(10, 2) NOT NULL,
    costo_por_km DECIMAL(8, 2) NOT NULL,
    costo_combustible DECIMAL(10, 2) DEFAULT 0,
    costo_estadia DECIMAL(10, 2) DEFAULT 0,
    horas_estadia DECIMAL(8, 2) DEFAULT 0,
    
    -- Total del tramo
    costo_total_tramo DECIMAL(10, 2) NOT NULL,
    
    fecha_calculo TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_costo_tramo_solicitud FOREIGN KEY (costo_solicitud_id) REFERENCES costo_solicitud(id) ON DELETE CASCADE,
    CONSTRAINT chk_costo_tramo_tipo CHECK (tipo IN ('ESTIMADO', 'REAL'))
);

-- Índices
CREATE INDEX IF NOT EXISTS idx_tarifa_base_vigencia ON tarifa_base(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX IF NOT EXISTS idx_tarifa_combustible_vigencia ON tarifa_combustible(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX IF NOT EXISTS idx_tarifa_estadia_vigencia ON tarifa_estadia(fecha_vigencia_desde, fecha_vigencia_hasta);
CREATE INDEX IF NOT EXISTS idx_costo_solicitud ON costo_solicitud(solicitud_id);
CREATE INDEX IF NOT EXISTS idx_costo_tipo ON costo_solicitud(tipo);
CREATE INDEX IF NOT EXISTS idx_costo_fecha ON costo_solicitud(fecha_calculo DESC);
CREATE INDEX IF NOT EXISTS idx_costo_tramo_tramo ON costo_tramo(tramo_id);
CREATE INDEX IF NOT EXISTS idx_costo_tramo_solicitud ON costo_tramo(costo_solicitud_id);

-- Datos de prueba - Tarifas vigentes
INSERT INTO tarifa_base (nombre, descripcion, costo_fijo_gestion, costo_adicional_por_tramo, fecha_vigencia_desde, activa) 
VALUES 
    ('Tarifa Estándar 2025', 'Tarifa base para servicios de transporte', 5000.00, 800.00, '2025-01-01', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO tarifa_combustible (precio_por_litro, fecha_vigencia_desde, activa) 
VALUES 
    (950.00, '2025-01-01', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO tarifa_estadia (costo_por_dia, costo_por_hora, fecha_vigencia_desde, activa) 
VALUES 
    (2500.00, 120.00, '2025-01-01', TRUE)
ON CONFLICT DO NOTHING;

INSERT INTO tarifa_peso_volumen (peso_minimo_kg, peso_maximo_kg, volumen_minimo_m3, volumen_maximo_m3, 
                                 multiplicador_costo, fecha_vigencia_desde, activa) 
VALUES 
    (0, 5000, 0, 15, 1.0, '2025-01-01', TRUE),
    (5001, 10000, 15.01, 30, 1.2, '2025-01-01', TRUE),
    (10001, 20000, 30.01, 60, 1.5, '2025-01-01', TRUE)
ON CONFLICT DO NOTHING;

COMMENT ON TABLE tarifa_base IS 'Tarifas base del servicio de transporte';
COMMENT ON TABLE tarifa_combustible IS 'Precio del combustible para cálculo de costos';
COMMENT ON TABLE tarifa_estadia IS 'Costos por estadía en depósitos';
COMMENT ON TABLE tarifa_peso_volumen IS 'Multiplicadores de costo según peso y volumen';
COMMENT ON TABLE costo_solicitud IS 'Costos calculados para cada solicitud';
COMMENT ON TABLE costo_tramo IS 'Costos por tramo de ruta';
