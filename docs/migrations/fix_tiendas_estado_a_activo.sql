-- ==============================================================================
-- Migración: Unificación de Estado a Booleano en Tabla 'tiendas'
-- Base de Datos: PostgreSQL (Supabase)
-- Objetivo: Eliminar la columna legacy 'estado varchar' ('Activa', 'Inactiva')
--           y estandarizar con la columna 'activo boolean'.
-- ==============================================================================

-- 1. Crear la columna 'activo' si aún no existe en la tabla tiendas
ALTER TABLE tiendas ADD COLUMN IF NOT EXISTS activo boolean DEFAULT true;

-- 2. Migrar los registros existentes mapeando strings ('Activa', 'activo', 'true') a true, el resto a false
UPDATE tiendas 
SET activo = CASE 
    WHEN LOWER(COALESCE(estado, '')) IN ('activa', 'activo', 'true', '1') THEN true 
    ELSE false 
END
WHERE activo IS NULL OR estado IS NOT NULL;

-- 3. Establecer restricciones NOT NULL y DEFAULT true para nuevas inserciones
ALTER TABLE tiendas ALTER COLUMN activo SET NOT NULL;
ALTER TABLE tiendas ALTER COLUMN activo SET DEFAULT true;

-- 4. Eliminar la columna legacy 'estado' de la base de datos
ALTER TABLE tiendas DROP COLUMN IF EXISTS estado;

-- 5. Verificar estructura final
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'tiendas' 
ORDER BY ordinal_position;
