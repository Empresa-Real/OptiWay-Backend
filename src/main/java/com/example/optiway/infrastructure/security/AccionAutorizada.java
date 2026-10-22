package com.example.optiway.infrastructure.security;

/**
 * Catálogo de acciones sujetas a autorización. Centraliza el texto de cada acción
 * para que los mensajes de error se compongan en un único lugar, en vez de
 * concatenar cadenas dispersas por los servicios. Añadir una acción nueva pasa
 * por declarar aquí su descripción, no por inventar un string en el servicio.
 */
public enum AccionAutorizada {

    CREAR_USUARIO("crear usuarios"),
    LISTAR_USUARIOS("consultar la lista de usuarios"),
    ELIMINAR_USUARIO("eliminar usuarios"),
    ACTUALIZAR_ROL("actualizar roles de usuario"),

    CREAR_TIENDA("crear tiendas"),
    ASIGNAR_ENCARGADO_TIENDA("asignar encargados a tiendas"),
    OPERAR_TIENDA("operar la tienda especificada"),
    CONSULTAR_INVENTARIO_TIENDA("consultar el inventario de esta tienda"),
    REGISTRAR_VENTA("registrar ventas en la tienda especificada"),

    CREAR_CENTRO_DISTRIBUCION("crear Centros de Distribución"),
    ASIGNAR_ENCARGADO_CENTRO_DISTRIBUCION("asignar encargados a Centros de Distribución"),
    OPERAR_CENTRO_DISTRIBUCION("operar el Centro de Distribución especificado"),
    CONSULTAR_INVENTARIO_CENTRO_DISTRIBUCION("consultar el inventario de este centro de distribución"),
    REGISTRAR_INGRESO("registrar ingresos en este Centro de Distribución"),

    CREAR_PRODUCTO("crear productos"),
    EDITAR_PRODUCTO("editar productos"),
    CAMBIAR_ESTADO_PRODUCTO("activar o desactivar productos");

    private final String descripcion;

    AccionAutorizada(String descripcion) {
        this.descripcion = descripcion;
    }

    /** Descripción legible de la acción, usada para componer los mensajes de error. */
    public String descripcion() {
        return descripcion;
    }
}
