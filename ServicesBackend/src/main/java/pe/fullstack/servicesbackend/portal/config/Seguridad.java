package pe.fullstack.servicesbackend.portal.config;
import org.springframework.context.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.*;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Arrays;
import pe.fullstack.servicesbackend.portal.servicio.IdentidadServicio;
@Configuration
public class Seguridad {
 @Bean PasswordEncoder encoder(){return new BCryptPasswordEncoder();}
 // Se admiten varios origenes exactos (separados por comas), sin usar el comodin "*".
 // Con el proxy de Vite el navegador hace peticiones al mismo origen y CORS no interviene.
 @Bean CorsConfigurationSource cors(@Value("${app.frontend-origins}") String origenes){
  CorsConfiguration c=new CorsConfiguration();
  c.setAllowedOrigins(Arrays.stream(origenes.split(",")).map(String::trim).filter(s -> !s.isEmpty()).distinct().toList());
  c.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
  c.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
  c.setMaxAge(3600L);
  UrlBasedCorsConfigurationSource s=new UrlBasedCorsConfigurationSource();s.registerCorsConfiguration("/**",c);return s;
 }
 @Bean SecurityFilterChain cadena(HttpSecurity http,IdentidadServicio identidad) throws Exception {
  http.csrf(csrf->csrf.disable()).cors(cors->{}).sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
   .authorizeHttpRequests(auth->auth
    .requestMatchers(HttpMethod.OPTIONS,"/**").permitAll()
    .requestMatchers(HttpMethod.GET,"/actuator/health","/api/servicios").permitAll()
    .requestMatchers("/api/auth/registro","/api/auth/cliente","/api/auth/personal").permitAll()
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/cliente/**").hasRole("CLIENTE")
    .requestMatchers("/api/auth/perfil","/api/auth/salir").authenticated()
    .anyRequest().denyAll())
   .exceptionHandling(e->e
    .authenticationEntryPoint((req,res,ex)->error(res,401,"Debes iniciar sesión"))
    .accessDeniedHandler((req,res,ex)->error(res,403,"No tienes permisos para esta sección")))
   .addFilterBefore(new TokenFiltro(identidad),UsernamePasswordAuthenticationFilter.class);
  return http.build();
 }
 private static void error(HttpServletResponse res,int status,String msg) throws IOException {
  res.setStatus(status);res.setCharacterEncoding("UTF-8");res.setContentType("application/json");
  res.getWriter().write("{\"message\":\""+msg+"\"}");
 }
 static class TokenFiltro extends OncePerRequestFilter {
  private final IdentidadServicio identidad;
  TokenFiltro(IdentidadServicio identidad){this.identidad=identidad;}
  @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain) throws ServletException,IOException {
   String header=request.getHeader("Authorization");
   if(header!=null&&header.startsWith("Bearer ")){
    String token=header.substring(7).trim();
    if(!token.isEmpty()) identidad.porToken(token).ifPresent(u->{
     var auth=new UsernamePasswordAuthenticationToken(u,null,List.of(new SimpleGrantedAuthority("ROLE_"+u.rol.name())));
     SecurityContextHolder.getContext().setAuthentication(auth);
    });
   }
   chain.doFilter(request,response);
  }
 }
}
