-- ==============================================================================
-- Migración idempotente para HU-21 (Órdenes de Reposición y auditoría).
--
-- 1) Permite NULL en ordenes_reposicion.creado_por (compatible con ON DELETE SET NULL).
-- 2) Agrega columnas de valores propuestos originales en recomendaciones_reposicion
--    para conservar trazabilidad cuando un planificador ajusta al aprobar.
-- 3) Limpia referencias huérfanas y recrea las FK de auditoría con ON DELETE SET NULL.
-- ==============================================================================

BEGIN;

-- 1) Permitir NULL en creado_por de ordenes_reposicion si la tabla ya existe
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'ordenes_reposicion' AND column_name = 'creado_por'
  ) THEN
    ALTER TABLE ordenes_reposicion ALTER COLUMN creado_por DROP NOT NULL;
  END IF;
END $$;

-- 2) Columnas para conservar valores originales propuestos tras un ajuste
ALTER TABLE IF EXISTS recomendaciones_reposicion
  ADD COLUMN IF NOT EXISTS cantidad_propuesta INTEGER,
  ADD COLUMN IF NOT EXISTS centro_distribucion_propuesto_id BIGINT;

-- 3) Limpiar referencias huérfanas antes de recrear FK
DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'recomendaciones_reposicion' AND column_name = 'resuelto_por'
  ) THEN
    UPDATE recomendaciones_reposicion r
    SET resuelto_por = NULL
    WHERE r.resuelto_por IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = r.resuelto_por);

    ALTER TABLE recomendaciones_reposicion DROP CONSTRAINT IF EXISTS fk_recomendaciones_resuelto_por;
    ALTER TABLE recomendaciones_reposicion
      ADD CONSTRAINT fk_recomendaciones_resuelto_por
      FOREIGN KEY (resuelto_por) REFERENCES usuarios(id) ON DELETE SET NULL;
  END IF;

  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_name = 'ordenes_reposicion' AND column_name = 'creado_por'
  ) THEN
    UPDATE ordenes_reposicion o
    SET creado_por = NULL
    WHERE o.creado_por IS NOT NULL
      AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = o.creado_por);

    ALTER TABLE ordenes_reposicion DROP CONSTRAINT IF EXISTS fk_ordenes_creador;
    ALTER TABLE ordenes_reposicion
      ADD CONSTRAINT fk_ordenes_creador
      FOREIGN KEY (creado_por) REFERENCES usuarios(id) ON DELETE SET NULL;
  END IF;
END $$;

COMMIT;
