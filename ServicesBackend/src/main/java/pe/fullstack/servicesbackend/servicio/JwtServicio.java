package pe.fullstack.servicesbackend.servicio;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.fullstack.servicesbackend.modelo.Usuario;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

/** Firma y valida JWT HS256. La revocación se controla con la tabla "sesiones". */
@Service
public class JwtServicio {

    private static final String EMISOR = "servicestech";

    private final SecretKey clave;

    public JwtServicio(@Value("${app.jwt.secret:}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(bytesDelSecreto(secreto));
    }

    /**
     * Acepta Base64 (p. ej. {@code openssl rand -base64 32} o generateValue de Render), Base64URL
     * o texto plano. En todos los casos exige al menos 32 bytes (256 bits).
     */
    static byte[] bytesDelSecreto(String secreto) {
        if (secreto == null || secreto.isBlank()) {
            throw new IllegalStateException("Define APP_JWT_SECRET (genera uno con: openssl rand -base64 32)");
        }
        String s = secreto.strip();
        byte[] bytes;
        try {
            bytes = Base64.getDecoder().decode(s);
        } catch (IllegalArgumentException base64) {
            try {
                bytes = Base64.getUrlDecoder().decode(s);
            } catch (IllegalArgumentException base64Url) {
                bytes = s.getBytes(StandardCharsets.UTF_8);
            }
        }
        if (bytes.length < 32) {
            throw new IllegalStateException("APP_JWT_SECRET debe tener al menos 32 bytes (256 bits)");
        }
        return bytes;
    }

    public String generar(Usuario usuario, Instant emitido, Instant expiracion) {
        return Jwts.builder()
                .issuer(EMISOR)
                .subject(usuario.getId().toString())
                .claim("roles", usuario.getRoles().stream().map(r -> r.getNombre().name()).sorted().toList())
                .issuedAt(Date.from(emitido))
                .expiration(Date.from(expiracion))
                .id(UUID.randomUUID().toString())
                .signWith(clave, Jwts.SIG.HS256)
                .compact();
    }

    public Optional<Claims> validar(String token) {
        try {
            return Optional.of(Jwts.parser().verifyWith(clave).requireIssuer(EMISOR).build()
                    .parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
