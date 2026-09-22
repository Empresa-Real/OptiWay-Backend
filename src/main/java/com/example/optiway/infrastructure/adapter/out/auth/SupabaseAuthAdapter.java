package com.example.optiway.infrastructure.adapter.out.auth;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.optiway.application.port.out.AuthPort;
import com.example.optiway.application.service.InvitacionAuthException;

@Component
public class SupabaseAuthAdapter implements AuthPort {

    private static final Logger log = LoggerFactory.getLogger(SupabaseAuthAdapter.class);

    private final RestClient restClient;
    private final String serviceRoleKey;
    private final String inviteRedirectUrl;

    public SupabaseAuthAdapter(
            @Value("${supabase.url}") String supabaseUrl,
            @Value("${supabase.service-role-key}") String serviceRoleKey,
            @Value("${supabase.invite-redirect-url}") String inviteRedirectUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(supabaseUrl)
                .build();
        this.serviceRoleKey = serviceRoleKey;
        this.inviteRedirectUrl = inviteRedirectUrl;
    }

    @Override
    public UUID invitar(String email) {
        if (serviceRoleKey.isBlank()) {
            throw new InvitacionAuthException("Falta configurar SUPABASE_SERVICE_ROLE_KEY");
        }

        try {
            log.info("Invitando usuario {} con redirect_to: {}", email, inviteRedirectUrl);
            AuthUserResponse response = restClient.post()
                    .uri("/auth/v1/invite?redirect_to={redirect_to}", inviteRedirectUrl)
                    .header("apikey", serviceRoleKey)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new InviteRequest(email, inviteRedirectUrl))
                    .retrieve()
                    .body(AuthUserResponse.class);

            if (response == null || response.id() == null) {
                throw new InvitacionAuthException("Supabase no devolvio el usuario invitado");
            }
            return response.id();
        } catch (InvitacionAuthException exception) {
            throw exception;
        } catch (org.springframework.web.client.RestClientResponseException exception) {
            // Si falló por límite de correos (429) o porque ya estaba registrado (422),
            // verificar si el usuario ya existe en Supabase Auth para reutilizar su UUID
            UUID usuarioExistenteId = buscarUsuarioPorEmailEnSupabase(email);
            if (usuarioExistenteId != null) {
                log.info("Usuario {} ya existe en Supabase Auth con ID {}. Reutilizando cuenta sin requerir nuevo correo.", email, usuarioExistenteId);
                return usuarioExistenteId;
            }

            String errorBody = exception.getResponseBodyAsString();
            log.error("Error al invitar usuario {} en Supabase [HTTP {}]: {}", email, exception.getStatusCode(), errorBody);
            throw new InvitacionAuthException("Error de Supabase Auth (" + exception.getStatusCode() + "): " + errorBody);
        } catch (RuntimeException exception) {
            log.error("Error inesperado al invitar usuario en Supabase: ", exception);
            throw new InvitacionAuthException("No se pudo invitar al usuario por correo: " + exception.getMessage());
        }
    }

    private UUID buscarUsuarioPorEmailEnSupabase(String email) {
        try {
            UsersListResponse list = restClient.get()
                    .uri("/auth/v1/admin/users")
                    .header("apikey", serviceRoleKey)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .retrieve()
                    .body(UsersListResponse.class);

            if (list != null && list.users() != null) {
                for (UserData u : list.users()) {
                    if (email.equalsIgnoreCase(u.email())) {
                        return u.id();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("No se pudo consultar listado de usuarios en Supabase Admin: {}", e.getMessage());
        }
        return null;
    }

    @Override
    public void eliminarUsuarioAuth(UUID authUserId) {
        if (authUserId == null) {
            return;
        }
        if (serviceRoleKey == null || serviceRoleKey.isBlank()) {
            throw new InvitacionAuthException("Falta configurar SUPABASE_SERVICE_ROLE_KEY");
        }

        try {
            log.info("Eliminando usuario en Supabase Auth: {}", authUserId);
            restClient.delete()
                    .uri("/auth/v1/admin/users/{userId}", authUserId.toString())
                    .header("apikey", serviceRoleKey)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Usuario {} eliminado exitosamente de Supabase Auth", authUserId);
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ignored) {
            log.warn("Usuario {} no existía en Supabase Auth al intentar eliminarlo", authUserId);
        } catch (org.springframework.web.client.RestClientResponseException exception) {
            String errorBody = exception.getResponseBodyAsString();
            log.error("Error al eliminar usuario {} en Supabase [HTTP {}]: {}", authUserId, exception.getStatusCode(), errorBody);
            throw new InvitacionAuthException("No se pudo eliminar el usuario de Supabase Auth: " + errorBody);
        } catch (RuntimeException exception) {
            log.error("Error inesperado al eliminar usuario {} en Supabase: ", authUserId, exception);
            throw new InvitacionAuthException("No se pudo eliminar el usuario de Supabase Auth: " + exception.getMessage());
        }
    }

    private record InviteRequest(String email, String redirect_to) {
    }

    private record AuthUserResponse(UUID id) {
    }

    private record UsersListResponse(java.util.List<UserData> users) {
    }

    private record UserData(UUID id, String email) {
    }
}