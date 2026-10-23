package com.authguard.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Documents every endpoint as requiring a Keycloak-issued bearer token, and adds the Authorize button to Swagger UI. */
@Configuration
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

    @Bean
    public OpenAPI authGuardOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AuthGuard API")
                        .version("v1")
                        .description("Centralized authorization service (RBAC + ABAC) backed by Keycloak OIDC."))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
