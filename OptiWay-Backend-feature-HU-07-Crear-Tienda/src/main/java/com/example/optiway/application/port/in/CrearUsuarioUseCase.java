package com.example.optiway.application.port.in;
import com.example.optiway.domain.model.Usuario;
import java.util.UUID;

public interface CrearUsuarioUseCase {
    Usuario crearUsuario(Usuario usuario, UUID authUserId);
}
