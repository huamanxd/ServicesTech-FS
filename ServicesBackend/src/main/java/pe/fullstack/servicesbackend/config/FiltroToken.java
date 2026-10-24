package pe.fullstack.servicesbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.fullstack.servicesbackend.servicio.IdentidadServicio;

import java.io.IOException;

/** Lee "Authorization: Bearer <jwt>" y, si la sesión es válida, autentica con los roles del usuario. */
@RequiredArgsConstructor
public class FiltroToken extends OncePerRequestFilter {

    private final IdentidadServicio identidad;

    public static String token(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        String token = header.substring(7).strip();
        return token.isEmpty() ? null : token;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String token = token(request);
        if (token != null) {
            identidad.porToken(token).ifPresent(u -> {
                var autoridades = u.getRoles().stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r.getNombre().name()))
                        .toList();
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(u, null, autoridades));
            });
        }
        chain.doFilter(request, response);
    }
}
