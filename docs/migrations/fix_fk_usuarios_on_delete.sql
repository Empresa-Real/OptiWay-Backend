-- ==============================================================================
-- Corrige las FK de usuarios para que al borrar un usuario las referencias
-- queden en NULL, en lugar de bloquear el borrado (NO ACTION).
--
-- Contexto: Hibernate (ddl-auto=update) creó estas FK sin ON DELETE y no pudo
-- crear fk_ingresos_usuario porque existía un ingreso con usuario_id huérfano.
-- Además, ingresos_mercancia.usuario_id era NOT NULL, incompatible con
-- ON DELETE SET NULL.
--
-- Idempotente: se puede ejecutar varias veces.
-- ==============================================================================

BEGIN;

-- 1) Permitir NULL en el histórico de ingresos (requisito de ON DELETE SET NULL)
ALTER TABLE ingresos_mercancia
  ALTER COLUMN usuario_id DROP NOT NULL;

-- 2) Limpiar referencias huérfanas que impiden crear las FK
UPDATE ingresos_mercancia i
SET usuario_id = NULL
WHERE i.usuario_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = i.usuario_id);

UPDATE tiendas t
SET encargado_id = NULL
WHERE t.encargado_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = t.encargado_id);

UPDATE centros_distribucion c
SET encargado_id = NULL
WHERE c.encargado_id IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM usuarios u WHERE u.id = c.encargado_id);

UPDATE usuarios u
SET creado_por = NULL
WHERE u.creado_por IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM usuarios c WHERE c.auth_user_id = u.creado_por);

-- 3) Recrear las FK con ON DELETE SET NULL
ALTER TABLE tiendas DROP CONSTRAINT IF EXISTS fk_tiendas_encargado;
ALTER TABLE tiendas
  ADD CONSTRAINT fk_tiendas_encargado
  FOREIGN KEY (encargado_id) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE centros_distribucion DROP CONSTRAINT IF EXISTS fk_centros_encargado;
ALTER TABLE centros_distribucion
  ADD CONSTRAINT fk_centros_encargado
  FOREIGN KEY (encargado_id) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE ingresos_mercancia DROP CONSTRAINT IF EXISTS fk_ingresos_usuario;
ALTER TABLE ingresos_mercancia
  ADD CONSTRAINT fk_ingresos_usuario
  FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE SET NULL;

ALTER TABLE usuarios DROP CONSTRAINT IF EXISTS fk_usuarios_creado_por;
ALTER TABLE usuarios
  ADD CONSTRAINT fk_usuarios_creado_por
  FOREIGN KEY (creado_por) REFERENCES usuarios(auth_user_id) ON DELETE SET NULL;

COMMIT;
