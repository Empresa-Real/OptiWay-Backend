package com.example.optiway.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.optiway.application.port.in.AsignarEncargadoTiendaUseCase;
import com.example.optiway.application.port.in.CrearTiendaUseCase;
import com.example.optiway.application.port.in.ObtenerTiendasUseCase;
import com.example.optiway.application.port.out.TiendaRepositoryPort;
import com.example.optiway.application.port.out.UsuarioRepositoryPort;
import com.example.optiway.domain.model.Rol;
import com.example.optiway.domain.model.Tienda;
import com.example.optiway.domain.model.Usuario;

@Service
public class TiendaService implements CrearTiendaUseCase, ObtenerTiendasUseCase, AsignarEncargadoTiendaUseCase {

    private final TiendaRepositoryPort tiendaRepositoryPort;
    private final UsuarioRepositoryPort usuarioRepositoryPort;

    public TiendaService(
            TiendaRepositoryPort tiendaRepositoryPort,
            UsuarioRepositoryPort usuarioRepositoryPort) {
        this.tiendaRepositoryPort = tiendaRepositoryPort;
        this.usuarioRepositoryPort = usuarioRepositoryPort;
    }

    @Override
    public Tienda crearTienda(Tienda tienda) {
        return crearTienda(tienda, null);
    }

    @Override
    public Tienda crearTienda(Tienda tienda, UUID authUserId) {
        if (authUserId != null) {
            Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                    .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
            if (usuario.getRol() != Rol.ADMINISTRADOR) {
                throw new AccesoDenegadoException("Solo un administrador puede crear tiendas");
            }
        }

        if (tienda.getNombre() == null || tienda.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la tienda es obligatorio.");
        }
        if (tienda.getDireccion() == null || tienda.getDireccion().trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es obligatoria.");
        }
        if (tienda.getCiudad() == null || tienda.getCiudad().trim().isEmpty()) {
            throw new IllegalArgumentException("La ciudad es obligatoria.");
        }

        tienda.setCodigo("TND-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        tienda.setEstado("Activa");

        return tiendaRepositoryPort.save(tienda);
    }

    @Override
    public List<Tienda> obtenerTiendas() {
        return tiendaRepositoryPort.findAll();
    }

    @Override
    public List<Tienda> obtenerTiendas(UUID authUserId) {
        if (authUserId == null) {
            return tiendaRepositoryPort.findAll();
        }

        Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.ADMINISTRADOR || usuario.getRol() == Rol.PLANIFICADOR) {
            return tiendaRepositoryPort.findAll();
        }

        if (usuario.getRol() == Rol.ENCARGADO_TIENDA) {
            return tiendaRepositoryPort.findAll().stream()
                    .filter(t -> usuario.getId().equals(t.getEncargadoId()))
                    .toList();
        }

        return List.of();
    }

    @Override
    public Tienda asignarEncargado(Long tiendaId, Long encargadoId) {
        return asignarEncargado(tiendaId, encargadoId, null);
    }

    @Override
    public Tienda asignarEncargado(Long tiendaId, Long encargadoId, UUID authUserId) {
        if (authUserId != null) {
            Usuario usuario = usuarioRepositoryPort.obtenerPorAuthUserId(authUserId)
                    .orElseThrow(() -> new AccesoDenegadoException("Usuario no encontrado"));
            if (usuario.getRol() != Rol.ADMINISTRADOR) {
                throw new AccesoDenegadoException("Solo un administrador puede asignar encargados a tiendas");
            }
        }

        Tienda tienda = tiendaRepositoryPort.obtenerPorId(tiendaId)
                .orElseThrow(() -> new IllegalArgumentException("La tienda no existe."));

        if (encargadoId != null) {
            Usuario encargado = usuarioRepositoryPort.obtenerPorID(encargadoId)
                    .orElseThrow(() -> new IllegalArgumentException("El usuario encargado no existe."));

            if (encargado.getRol() != Rol.ENCARGADO_TIENDA) {
                throw new IllegalArgumentException(
                        "Solo se puede asignar una tienda a un usuario con rol Encargado de tienda (Rol actual: " 
                        + encargado.getRol() + ").");
            }
        }

        tienda.setEncargadoId(encargadoId);
        return tiendaRepositoryPort.save(tienda);
    }
}