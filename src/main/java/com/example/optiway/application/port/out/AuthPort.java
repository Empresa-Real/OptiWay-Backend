package com.example.optiway.application.port.out;

import java.util.UUID;

/**
 * Puerto de salida para la gestión administrativa de identidades en Supabase Auth.
 * 
 * Nota: Este puerto se utiliza exclusivamente para operaciones administrativas
 * (invitar usuarios, revocar/eliminar cuentas en auth.users). El inicio de sesión (login)
 * lo gestiona directamente el cliente con Supabase Auth y se valida en el backend mediante JWT.
 */
public interface AuthPort {

    /**
     * Invita a un nuevo usuario vía correo electrónico generando su cuenta en Supabase Auth.
     * Solo debe ser invocado por administradores autenticados.
     */
    UUID invitar(String email);

    /**
     * Elimina administrativamente la cuenta de un usuario en Supabase Auth.
     * Solo debe ser invocado por administradores autenticados.
     */
    void eliminarUsuarioAuth(UUID authUserId);
}
