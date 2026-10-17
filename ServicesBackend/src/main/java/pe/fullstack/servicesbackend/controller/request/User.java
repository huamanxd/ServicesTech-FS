package pe.fullstack.servicesbackend.controller.request;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

    private Integer id;

    private String username;

    @JsonIgnore // Evita exponer contraseñas si se usa este DTO heredado en el futuro.
    private String password;

    private String email;

}
