package com.example.optiway.infrastructure.config;

import com.example.optiway.infrastructure.security.AuthUserIdArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthUserIdArgumentResolver authUserIdArgumentResolver;

    public WebMvcConfig(AuthUserIdArgumentResolver authUserIdArgumentResolver) {
        this.authUserIdArgumentResolver = authUserIdArgumentResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserIdArgumentResolver);
    }
}
