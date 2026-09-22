package com.example.optiway.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.port.in.AsignarEncargadoCDUseCase;
import com.example.optiway.application.port.in.CrearCentroDistribucionUseCase;
import com.example.optiway.application.port.out.CentroDistribucionRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.CentroDistribucion;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Usuario;

@Service
public class CentroDistribucionService implements CrearCentroDistribucionUseCase, AsignarEncargadoCDUseCase {

    private final CentroDistribucionRepositoryPort centroDistribucionRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public CentroDistribucionService(
            CentroDistribucionRepositoryPort centroDistribucionRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort) {
        this.centroDistribucionRepositoryPort = centroDistribucionRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public CentroDistribucion crearCentroDistribucion(CentroDistribucion centroDistribucion) {
        return crearCentroDistribucion(centroDistribucion, null);
    }

    @Override
    public CentroDistribucion crearCentroDistribucion(CentroDistribucion centroDistribucion, UUID authUserId) {
        if (authUserId != null) {
            Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                    .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
            if (usuario.getRol() != Rol.ADMINISTRADOR) {
                throw new AccesoDenegadoException("Solo un administrador puede crear Centros de Distribución");
            }
        }

        if (centroDistribucion.getDireccion() == null || centroDistribucion.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección del centro de distribución es obligatoria.");
        }

        if (centroDistribucion.getNombre() == null || centroDistribucion.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del centro de distribución es obligatorio.");
        }

        String codigoUnico = "CD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        centroDistribucion.setCodigo(codigoUnico);

        return centroDistribucionRepositoryPort.guardar(centroDistribucion);
    }

    @Override
    public List<CentroDistribucion> listarCentrosDistribucion() {
        return centroDistribucionRepositoryPort.obtenerTodos();
    }

    @Override
    public List<CentroDistribucion> listarCentrosDistribucion(UUID authUserId) {
        if (authUserId == null) {
            return centroDistribucionRepositoryPort.obtenerTodos();
        }

        Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.PLANIFICADOR) {
            return centroDistribucionRepositoryPort.obtenerTodos();
        }

        if (usuario.getRol() == Rol.ENCARGADO_CD) {
            return centroDistribucionRepositoryPort.obtenerTodos().stream()
                    .filter(c -> usuario.getId().equals(c.getEncargadoId()))
                    .toList();
        }

        return List.of();
    }

    @Override
    public CentroDistribucion asignarEncargado(Long centroId, Long encargadoId) {
        return asignarEncargado(centroId, encargadoId, null);
    }

    @Override
    public CentroDistribucion asignarEncargado(Long centroId, Long encargadoId, UUID authUserId) {
        if (authUserId != null) {
            Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                    .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
            if (usuario.getRol() != Rol.ADMINISTRADOR) {
                throw new AccesoDenegadoException("Solo un administrador puede asignar encargados a Centros de Distribución");
            }
        }

        CentroDistribucion centro = centroDistribucionRepositoryPort.obtenerPorId(centroId)
                .orElseThrow(() -> new IllegalArgumentException("El centro de distribución no existe."));

        if (encargadoId != null) {
            Usuario encargado = usuarioRepositoryPort.obtenerPorID(encargadoId)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario encargado no existe."));

            if (encargado.getRol() != Rol.ENCARGADO_CD) {
                throw new IllegalArgumentException(
                        "Solo se puede asignar un centro de distribución a un usuario con rol Encargado de CD (Rol actual: " 
                        + encargado.getRol() + ").");
            }
        }

        centro.setEncargadoId(encargadoId);
        return centroDistribucionRepositoryPort.guardar(centro);
    }
}