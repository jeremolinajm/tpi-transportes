-- Agregar columna costo_real_total a la tabla ruta
-- Este script debe ejecutarse manualmente en la base de datos logistics_db

ALTER TABLE ruta
ADD COLUMN IF NOT EXISTS costo_real_total DECIMAL(12, 2);

COMMENT ON COLUMN ruta.costo_real_total IS 'Costo real total de la ruta calculado después de la entrega';
