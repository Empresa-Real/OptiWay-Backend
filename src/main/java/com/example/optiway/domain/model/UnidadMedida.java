package com.example.optiway.domain.model;

import java.util.Locale;
import java.util.Map;

/**
 * Catálogo estándar de unidades de medida permitidas para productos.
 */
public enum UnidadMedida {
    UNIDAD,
    KG,
    G,
    LITRO,
    ML,
    CAJA,
    PAQUETE,
    DOCENA;

    private static final Map<String, UnidadMedida> ALIAS = Map.ofEntries(
            Map.entry("unidad", UNIDAD),
            Map.entry("unidades", UNIDAD),
            Map.entry("u", UNIDAD),
            Map.entry("und", UNIDAD),
            Map.entry("unid", UNIDAD),
            Map.entry("kg", KG),
            Map.entry("kilo", KG),
            Map.entry("kilos", KG),
            Map.entry("kilogramo", KG),
            Map.entry("kilogramos", KG),
            Map.entry("g", G),
            Map.entry("gr", G),
            Map.entry("gramo", G),
            Map.entry("gramos", G),
            Map.entry("litro", LITRO),
            Map.entry("litros", LITRO),
            Map.entry("l", LITRO),
            Map.entry("lt", LITRO),
            Map.entry("lts", LITRO),
            Map.entry("ml", ML),
            Map.entry("mililitro", ML),
            Map.entry("mililitros", ML),
            Map.entry("caja", CAJA),
            Map.entry("cajas", CAJA),
            Map.entry("paquete", PAQUETE),
            Map.entry("paquetes", PAQUETE),
            Map.entry("pq", PAQUETE),
            Map.entry("paq", PAQUETE),
            Map.entry("docena", DOCENA),
            Map.entry("docenas", DOCENA)
    );

    public static String normalizar(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException("La unidad de medida es obligatoria.");
        }
        String clave = valor.trim().toLowerCase(Locale.ROOT);
        UnidadMedida unidad = ALIAS.get(clave);
        if (unidad == null) {
            throw new IllegalArgumentException(
                    "Unidad de medida no válida: '" + valor.trim()
                            + "'. Valores permitidos: UNIDAD, KG, G, LITRO, ML, CAJA, PAQUETE, DOCENA.");
        }
        return unidad.name();
    }
}
