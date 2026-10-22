package com.example.optiway.infrastructure.security;

import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.UUID;

/**
 * Resuelve el parámetro {@link AuthUserId} a partir del JWT autenticado.
 *
 * <p>Contrato estricto: un endpoint que declare {@code @AuthUserId} exige
 * identidad. Si no hay autenticación, el principal no es un JWT, el claim
 * {@code sub} está ausente o no es un UUID válido, se lanza
 * {@link AutenticacionRequeridaException} (401) en lugar de devolver
 * {@code null}. Devolver {@code null} de forma silenciosa hacía ambigua la
 * causa del fallo y permitía que la petición llegara a la capa de aplicación
 * sin identidad.
 */
@Component
public class AuthUserIdArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(AuthUserId.class)
                && UUID.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter,
                                  ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest,
                                  WebDataBinderFactory binderFactory) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AutenticacionRequeridaException("Autenticación requerida");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) {
            throw new AutenticacionRequeridaException(
                    "La autenticación no contiene un token JWT válido");
        }

        String subject = jwt.getSubject();
        if (subject == null || subject.isBlank()) {
            throw new AutenticacionRequeridaException(
                    "El token no contiene la identidad del usuario (claim 'sub')");
        }

        try {
            return UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new AutenticacionRequeridaException(
                    "La identidad del token no tiene un formato válido");
        }
    }
}
