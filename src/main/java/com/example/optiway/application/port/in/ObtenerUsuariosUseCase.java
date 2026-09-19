package com.example.optiway.application.port.in;
import com.example.optiway.domain.model.Usuario;

import java.util.List;

public interface ObtenerUsuariosUseCase {

    List<Usuario> obtenerUsuarios();

}