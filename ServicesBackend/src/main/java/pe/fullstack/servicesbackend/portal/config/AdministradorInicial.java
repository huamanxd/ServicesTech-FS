package pe.fullstack.servicesbackend.portal.config;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import pe.fullstack.servicesbackend.portal.modelo.*;
import pe.fullstack.servicesbackend.portal.repositorio.UsuarioRepositorio;
import pe.fullstack.servicesbackend.portal.servicio.IdentidadServicio;
@Component
public class AdministradorInicial implements CommandLineRunner {
 private final UsuarioRepositorio repo;private final PasswordEncoder encoder;
 @Value("${app.admin.email:}") private String email;
 @Value("${app.admin.password:}") private String clave;
 public AdministradorInicial(UsuarioRepositorio repo,PasswordEncoder encoder){this.repo=repo;this.encoder=encoder;}
 @Override public void run(String... args){
  if(email.isBlank()&&clave.isBlank())return;
  if(email.isBlank()||clave.length()<12)throw new IllegalArgumentException("ADMIN_EMAIL y ADMIN_PASSWORD (mínimo 12 caracteres) son obligatorios en conjunto");
  String e=IdentidadServicio.normaliza(email);
  if(repo.findByEmailIgnoreCase(e).isPresent())return; // Nunca cambia una cuenta existente de rol.
  Usuario u=new Usuario();u.nombres="Administrador";u.apellidos="Inicial";u.email=e;
  u.claveHash=encoder.encode(clave);u.rol=Rol.ADMIN;repo.save(u);
 }
}
