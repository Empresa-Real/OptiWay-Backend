package com.example.optiway.infrastructure.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inyecta automáticamente el UUID del usuario autenticado (extraído de forma segura del JWT de Supabase)
 * directamente como argumento en los endpoints de los controladores REST.
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AuthUserId {
}
