-- ==============================================================================
-- producto_ediciones: pasa de guardar el UUID de Supabase (usuario_auth_id) a
-- guardar el id local (usuario_id), con FK real a usuarios(id) y ON DELETE
-- SET NULL, igual que tiendas, centros y ingresos.
--
-- Idempotente: se puede ejecutar varias veces.
-- ==============================================================================

BEGIN;

ALTER TABLE producto_ediciones
  ADD COLUMN IF NOT EXISTS usuario_id BIGINT;

DO $$
BEGIN
  IF EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'producto_ediciones'
      AND column_name = 'usuario_auth_id'
  ) THEN
    -- Backfill: UUID de Supabase -> id local, cuando el usuario todavía existe
    UPDATE producto_ediciones pe
    SET usuario_id = u.id
    FROM usuarios u
    WHERE pe.usuario_id IS NULL
      AND u.auth_user_id = pe.usuario_auth_id;

    ALTER TABLE producto_ediciones DROP COLUMN usuario_auth_id;
  END IF;
END $$;

ALTER TABLE producto_ediciones
  DROP CONSTRAINT IF EXISTS fk_producto_ediciones_usuario;

ALTER TABLE producto_ediciones
  ADD CONSTRAINT fk_producto_ediciones_usuario
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL;

COMMIT;
