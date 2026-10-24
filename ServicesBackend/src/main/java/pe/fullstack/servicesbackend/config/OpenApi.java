package pe.fullstack.servicesbackend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/** Documentación en /swagger-ui.html y /v3/api-docs. Para probar: POST /api/auth/... y "Authorize" con el token. */
@Configuration
@OpenAPIDefinition(info = @Info(title = "ServicesTech API", version = "1.0.0",
        description = "Solicitudes, órdenes de servicio, técnicos, satisfacción y dashboards."))
@SecurityScheme(name = OpenApi.BEARER, type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApi {
    public static final String BEARER = "bearer";
}
