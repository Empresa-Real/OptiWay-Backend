package com.example.optiway.infrastructure.config;

import com.example.optiway.infrastructure.observability.MemoriaAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.Logger;
import jakarta.annotation.PostConstruct;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

/** Adjunta el appender en memoria al logger raiz de Logback. */
@Configuration
public class LogMemoriaConfig {

    @PostConstruct
    public void adjuntarAppender() {
        LoggerContext contexto = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger raiz = contexto.getLogger(Logger.ROOT_LOGGER_NAME);
        if (raiz.getAppender("MEMORIA") != null) {
            return;
        }
        MemoriaAppender appender = new MemoriaAppender();
        appender.setName("MEMORIA");
        appender.setContext(contexto);
        appender.start();
        raiz.addAppender(appender);
    }
}
