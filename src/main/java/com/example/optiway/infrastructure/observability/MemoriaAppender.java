package com.example.optiway.infrastructure.observability;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

/** Appender de Logback que alimenta el buffer en memoria del panel /admin. */
public class MemoriaAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent evento) {
        LogBuffer.registrar(
                evento.getTimeStamp(),
                evento.getLevel().toString(),
                evento.getThreadName(),
                evento.getLoggerName(),
                evento.getFormattedMessage());
    }
}
