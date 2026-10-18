package com.example.optiway.infrastructure.config;

import com.example.optiway.infrastructure.security.AuthUserId;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "springdoc.swagger-ui.enabled", havingValue = "true", matchIfMissing = true)
public class OpenApiConfig {

    static {
        SpringDocUtils.getConfig().addAnnotationsToIgnore(AuthUserId.class);
    }

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("OptiWay Backend API")
                        .version("1.0.0")
                        .description("API REST para la plataforma logística OptiWay: Gestión de Tiendas, Centros de Distribución, Inventario, Mercancía y Ventas.")
                        .contact(new Contact().name("Equipo de Desarrollo SupplyCore"))
                        .license(new License().name("Apache 2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Ingresa el Bearer JWT proporcionado por Supabase Auth para autorizar peticiones protegidas.")));
    }
}
