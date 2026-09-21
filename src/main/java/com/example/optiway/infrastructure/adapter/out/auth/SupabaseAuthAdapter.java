package com.example.optiway.infrastructure.adapter.out.auth;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.optiway.application.port.out.AuthPort;
import com.example.optiway.application.service.InvitacionAuthException;

@Component
public class SupabaseAuthAdapter implements AuthPort {

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
            AuthUserResponse response = restClient.post()
                    .uri("/auth/v1/invite")
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
        } catch (RuntimeException exception) {
            throw new InvitacionAuthException("No se pudo invitar al usuario por correo");
        }
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
            restClient.delete()
                    .uri("/auth/v1/admin/users/{userId}", authUserId.toString())
                    .header("apikey", serviceRoleKey)
                    .header("Authorization", "Bearer " + serviceRoleKey)
                    .retrieve()
                    .toBodilessEntity();
        } catch (org.springframework.web.client.HttpClientErrorException.NotFound ignored) {
            // Si el usuario ya no existe en Supabase Auth, se continúa para limpiar la BD local
        } catch (RuntimeException exception) {
            throw new InvitacionAuthException("No se pudo eliminar el usuario de Supabase Auth: " + exception.getMessage());
        }
    }

    private record InviteRequest(String email, String redirect_to) {
    }

    private record AuthUserResponse(UUID id) {
    }
}