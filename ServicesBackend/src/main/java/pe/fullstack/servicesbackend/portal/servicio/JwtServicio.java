package pe.fullstack.servicesbackend.portal.servicio;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import pe.fullstack.servicesbackend.portal.modelo.Usuario;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtServicio {

    private static final String EMISOR = "servicestech";

    private final SecretKey clave;

    public JwtServicio(
        @Value("${app.jwt.secret}") String secretoBase64
    ) {
        try {
            byte[] bytes = Decoders.BASE64.decode(secretoBase64);
            this.clave = Keys.hmacShaKeyFor(bytes);
        } catch (RuntimeException ex) {
            throw new IllegalStateException(
                "APP_JWT_SECRET debe ser una clave Base64 segura de al menos 32 bytes",
                ex
            );
        }
    }

    public String generar(Usuario usuario, Instant expiracion) {

        return Jwts.builder()
            .issuer(EMISOR)
            .subject(usuario.id.toString())
            .claim("rol", usuario.rol.name())
            .issuedAt(new Date())
            .expiration(Date.from(expiracion))
            .id(UUID.randomUUID().toString())
            .signWith(clave, Jwts.SIG.HS256)
            .compact();
    }

    public Optional<Claims> validar(String token) {
        try {
            Claims datos = Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(EMISOR)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            return Optional.of(datos);

        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
