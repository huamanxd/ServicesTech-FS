package pe.fullstack.servicesbackend.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import pe.fullstack.servicesbackend.servicio.IdentidadServicio;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Autorización por rol en el servidor (la del frontend es solo de navegación).
 * Todo lo no declarado queda denegado.
 */
@Configuration
public class Seguridad {

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Orígenes permitidos separados por coma (FRONTEND_ORIGINS). Admite comodines, por ejemplo
     * https://servicestech-*.vercel.app para las vistas previas de Vercel. Nunca se usa "*".
     * Con Vite y Nginx la cabecera Origin no llega al backend; con Vercel sí, por eso en Render
     * hay que declarar el dominio de Vercel.
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource(@Value("${app.frontend-origins:}") String origenes) {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(Arrays.stream(origenes.split(",")).map(String::strip)
                .filter(s -> !s.isEmpty() && !s.equals("*")).distinct().toList());
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        c.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", c);
        return fuente;
    }

    @Bean
    SecurityFilterChain cadena(HttpSecurity http, IdentidadServicio identidad) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> { })
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/api/servicios", "/api/prioridades").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/registro", "/api/auth/cliente", "/api/auth/personal").permitAll()
                        .requestMatchers("/api/auth/perfil", "/api/auth/salir").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/api/tecnico/**").hasRole("TECNICO")
                        .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> error(res, 401, "Debes iniciar sesión"))
                        .accessDeniedHandler((req, res, ex) -> error(res, 403, "No tienes permisos para esta sección")))
                .addFilterBefore(new FiltroToken(identidad), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private static void error(HttpServletResponse res, int status, String mensaje) throws IOException {
        res.setStatus(status);
        res.setCharacterEncoding("UTF-8");
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.getWriter().write("{\"message\":\"" + mensaje + "\"}");
    }
}
