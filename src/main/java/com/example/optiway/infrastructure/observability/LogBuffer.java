package com.example.optiway.infrastructure.observability;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Buffer circular en memoria con las ultimas lineas de log.
 * Se usa cuando el appender de archivo no esta disponible (Render: filesystem efimero).
 */
public final class LogBuffer {

    private static final int MAX = 500;
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneOffset.UTC);
    private static final List<String> LINEAS = Collections.synchronizedList(new LinkedList<>());

    private LogBuffer() {
    }

    public static void agregar(String linea) {
        synchronized (LINEAS) {
            if (LINEAS.size() >= MAX) {
                LINEAS.remove(0);
            }
            LINEAS.add(linea);
        }
    }

    public static List<String> lineas(int cantidad) {
        synchronized (LINEAS) {
            int desde = Math.max(0, LINEAS.size() - Math.max(1, Math.min(cantidad, MAX)));
            return List.copyOf(LINEAS.subList(desde, LINEAS.size()));
        }
    }

    public static void registrar(
            long timestamp, String nivel, String hilo, String logger, String mensaje) {
        agregar(FORMATO.format(Instant.ofEpochMilli(timestamp))
                + " " + nivel + " [" + hilo + "] " + logger + " - " + mensaje);
    }
}
