-- ==============================================================================
-- Migración: Agregar CHECK constraint para validar el rol de usuarios
-- Base de Datos: PostgreSQL (Supabase)
-- Objetivo: Garantizar que solo se permitan valores válidos del enum Rol
--           ('ADMINISTRADOR', 'ENCARGADO_TIENDA', 'ENCARGADOR_CD', 'PLANIFICADOR')
-- Nota: Se guarda como STRING con @Enumerated(EnumType.STRING) en la entidad JPA
-- ==============================================================================

-- 1. Verificar que no existan roles inválidos antes de agregar la restricción
-- (Si esta consulta devuelve filas, corregirlos antes de aplicar la migración)
-- SELECT id, email, rol FROM usuarios WHERE rol NOT IN ('ADMINISTRADOR', 'ENCARGADO_TIENDA', 'ENCARGADO_CD', 'PLANIFICADOR');

-- 2. Agregar la restricción CHECK
ALTER TABLE usuarios
  ADD CONSTRAINT chk_usuarios_rol
  CHECK (rol IN ('ADMINISTRADOR', 'ENCARGADO_TIENDA', 'ENCARGADO_CD', 'PLANIFICADOR'));

-- 3. Verificar la restricción creada
SELECT conname, pg_get_constraintdef(c.oid) AS definition
FROM pg_constraint c
JOIN pg_class t ON t.oid = c.conrelid
WHERE t.relname = 'usuarios' AND c.contype = 'c';
