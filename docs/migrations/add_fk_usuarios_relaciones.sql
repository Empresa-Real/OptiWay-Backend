-- ==============================================================================
-- Migración: Integridad referencial entre usuarios y tablas relacionadas
-- Base de Datos: PostgreSQL (Supabase)
-- Objetivo: Añadir claves foráneas con reglas ON DELETE definidas
--           para evitar borrados inconsistentes y limpiar referencias huérfanas
-- ==============================================================================

-- NOTA: Antes de ejecutar esta migración conviene revisar datos huérfanos
-- para evitar errores al crear las FK:
--
-- SELECT id FROM tiendas WHERE encargado_id IS NOT NULL AND encargado_id NOT IN (SELECT id FROM usuarios);
-- SELECT id FROM centros_distribucion WHERE encargado_id IS NOT NULL AND encargado_id NOT IN (SELECT id FROM usuarios);
-- SELECT id FROM ingresos_mercancia WHERE usuario_id IS NOT NULL AND usuario_id NOT IN (SELECT id FROM usuarios);
-- SELECT id FROM usuarios WHERE creado_por IS NOT NULL AND creado_por NOT IN (SELECT auth_user_id FROM usuarios);

-- 1) LIMPIAR HUÉRFANOS ANTES DE AÑADIR FKs
-- Encargados de tiendas/CD: al borrar el usuario, estas referencias se desasignarán
UPDATE tiendas
SET encargado_id = NULL
WHERE encargado_id IS NOT NULL
  AND encargado_id NOT IN (SELECT id FROM usuarios);

UPDATE centros_distribucion
SET encargado_id = NULL
WHERE encargado_id IS NOT NULL
  AND encargado_id NOT IN (SELECT id FROM usuarios);

-- Historial: si queda usuarioId huérfano, se deja NULL (no se borra el ingreso)
UPDATE ingresos_mercancia
SET usuario_id = NULL
WHERE usuario_id IS NOT NULL
  AND usuario_id NOT IN (SELECT id FROM usuarios);

-- creado_por referencia auth_user_id. Si queda huérfano, se deja NULL
UPDATE usuarios
SET creado_por = NULL
WHERE creado_por IS NOT NULL
  AND creado_por NOT IN (SELECT auth_user_id FROM usuarios);

-- 2) AÑADIR CLAVES FORÁNEAS

-- tiendas.encargado_id -> usuarios.id
-- Al eliminar un usuario encargado, se desasigna automáticamente
ALTER TABLE tiendas
  ADD CONSTRAINT fk_tiendas_encargado
  FOREIGN KEY (encargado_id)
  REFERENCES usuarios(id)
  ON DELETE SET NULL;

-- centros_distribucion.encargado_id -> usuarios.id
-- Al eliminar un usuario encargado, se desasigna automáticamente
ALTER TABLE centros_distribucion
  ADD CONSTRAINT fk_centros_encargado
  FOREIGN KEY (encargado_id)
  REFERENCES usuarios(id)
  ON DELETE SET NULL;

-- ingresos_mercancia.usuario_id -> usuarios.id
-- Se conserva el ingreso histórico, pero se elimina la referencia al usuario
ALTER TABLE ingresos_mercancia
  ADD CONSTRAINT fk_ingresos_usuario
  FOREIGN KEY (usuario_id)
  REFERENCES usuarios(id)
  ON DELETE SET NULL;

-- usuarios.creado_por -> usuarios.auth_user_id
-- Se conserva el registro, pero se pierde la referencia al creador
ALTER TABLE usuarios
  ADD CONSTRAINT fk_usuarios_creado_por
  FOREIGN KEY (creado_por)
  REFERENCES usuarios(auth_user_id)
  ON DELETE SET NULL;

-- 3) VERIFICAR FKs CREADAS
SELECT
  tc.constraint_name,
  tc.table_name,
  kcu.column_name,
  ccu.table_name AS foreign_table_name,
  ccu.column_name AS foreign_column_name,
  rc.delete_rule
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
  AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
  AND ccu.table_schema = tc.table_schema
JOIN information_schema.referential_constraints AS rc
  ON tc.constraint_name = rc.constraint_name
  AND tc.table_schema = rc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_name IN ('tiendas', 'centros_distribucion', 'ingresos_mercancia', 'usuarios')
ORDER BY tc.table_name, tc.constraint_name;
