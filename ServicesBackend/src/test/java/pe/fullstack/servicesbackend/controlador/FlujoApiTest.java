package pe.fullstack.servicesbackend.controlador;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integración real: Spring Security + controladores + servicios + JPA + Flyway sobre PostgreSQL
 * (en CI lo provee el servicio postgres del workflow; en local, DB_URL/DB_USER/DB_PASSWORD).
 */
@SpringBootTest(properties = {
        "app.jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
        "app.admin.email=admin-pruebas@servicestech.test",
        "app.admin.password=AdminPruebas2026!"})
@AutoConfigureMockMvc
class FlujoApiTest {

    @Autowired
    MockMvc mvc;

    private final String sufijo = String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));

    private ResultActions enviar(String url, String token, String json) throws Exception {
        var req = post(url).contentType(MediaType.APPLICATION_JSON).content(json == null ? "" : json);
        if (token != null) req.header("Authorization", "Bearer " + token);
        return mvc.perform(req);
    }

    private ResultActions consultar(String url, String token) throws Exception {
        var req = get(url);
        if (token != null) req.header("Authorization", "Bearer " + token);
        return mvc.perform(req);
    }

    private static String leer(ResultActions r, String ruta) throws Exception {
        return String.valueOf((Object) JsonPath.read(r.andReturn().getResponse().getContentAsString(), ruta));
    }

    private String login(String portal, String email, String clave) throws Exception {
        return leer(enviar("/api/auth/" + portal, null, "{\"email\":\"%s\",\"clave\":\"%s\"}".formatted(email, clave))
                .andExpect(status().isOk()), "$.token");
    }

    @Test
    void seguridadYValidaciones() throws Exception {
        consultar("/api/servicios", null).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(12))));
        consultar("/api/prioridades", null).andExpect(status().isOk()).andExpect(jsonPath("$[*].nombre", contains("BAJA", "MEDIA", "ALTA", "CRITICA")));
        consultar("/api/admin/dashboard", null).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.message").exists());
        enviar("/api/auth/registro", null, "{malformado").andExpect(status().isBadRequest());
        enviar("/api/auth/registro", null, "{\"nombres\":\"A\",\"email\":\"x\"}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fields.dni").exists())
                .andExpect(jsonPath("$.fields.email").exists());

        String cliente = registrarCliente();
        consultar("/api/admin/dashboard", cliente).andExpect(status().isForbidden());
        consultar("/api/tecnico/ordenes", cliente).andExpect(status().isForbidden());
        consultar("/api/admin/ordenes/abc/historial", login("personal", "admin-pruebas@servicestech.test", "AdminPruebas2026!"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void flujoCompletoDeAtencion() throws Exception {
        String admin = login("personal", "admin-pruebas@servicestech.test", "AdminPruebas2026!");
        String cliente = registrarCliente();

        String servicioId = leer(consultar("/api/servicios", null), "$[1].id");
        String prioridadId = leer(consultar("/api/prioridades", null), "$[3].id");
        var solicitud = enviar("/api/cliente/solicitudes", cliente, """
                {"servicioId":%s,"prioridadId":%s,"asunto":"Servidor caído","descripcion":"El servidor principal no responde desde la mañana"}
                """.formatted(servicioId, prioridadId))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.prioridad").value("CRITICA"));
        String sid = leer(solicitud, "$.id");

        // No se puede generar orden antes de aprobar.
        enviar("/api/admin/solicitudes/" + sid + "/orden", admin, "{}").andExpect(status().isConflict());
        enviar("/api/admin/solicitudes/" + sid + "/revision", admin, "{\"estado\":\"APROBADA\"}")
                .andExpect(jsonPath("$.estado").value("APROBADA"));

        String tecnicoId = leer(enviar("/api/admin/usuarios", admin, """
                {"nombres":"Luis","apellidos":"Huaman","dni":"%s","email":"tec-%s@servicestech.test",
                 "telefono":"987654300","clave":"ClaveTecnico2026","rol":"TECNICO"}""".formatted(dniDistinto(), sufijo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles", contains("TECNICO"))), "$.id");

        var orden = enviar("/api/admin/solicitudes/" + sid + "/orden", admin, "{\"tecnicoId\":" + tecnicoId + "}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ASIGNADA"))
                .andExpect(jsonPath("$.tecnico.id").value(Integer.parseInt(tecnicoId)));
        String oid = leer(orden, "$.id");
        enviar("/api/cliente/solicitudes/" + sid + "/cancelar", cliente, null).andExpect(status().isConflict());

        String tecnico = login("personal", "tec-" + sufijo + "@servicestech.test", "ClaveTecnico2026");
        enviar("/api/tecnico/ordenes/" + oid + "/solucion", tecnico, "{\"descripcion\":\"Sin iniciar todavía la atención\"}")
                .andExpect(status().isConflict());
        enviar("/api/tecnico/ordenes/" + oid + "/iniciar", tecnico, null).andExpect(jsonPath("$.estado").value("EN_ATENCION"));
        enviar("/api/tecnico/ordenes/" + oid + "/solucion", tecnico, "{\"descripcion\":\"Se reinició el servicio y se cambió el disco\"}")
                .andExpect(jsonPath("$.estado").value("RESUELTA"))
                .andExpect(jsonPath("$.solucion.tecnico.id").value(Integer.parseInt(tecnicoId)));

        enviar("/api/cliente/ordenes/" + oid + "/satisfaccion", cliente, "{\"calificacion\":6}").andExpect(status().isBadRequest());
        enviar("/api/cliente/ordenes/" + oid + "/satisfaccion", cliente, "{\"calificacion\":4,\"comentario\":\"Bien\"}")
                .andExpect(jsonPath("$.estado").value("CERRADA"))
                .andExpect(jsonPath("$.fechaCierre").exists());
        enviar("/api/cliente/ordenes/" + oid + "/satisfaccion", cliente, "{\"calificacion\":5}").andExpect(status().isConflict());

        consultar("/api/admin/ordenes/" + oid + "/historial", admin)
                .andExpect(jsonPath("$[*].estado", contains("PENDIENTE", "ASIGNADA", "EN_ATENCION", "RESUELTA", "CERRADA")));
        consultar("/api/cliente/dashboard", cliente)
                .andExpect(jsonPath("$.indicadores.resueltas").value(1))
                .andExpect(jsonPath("$.indicadores.satisfaccionPromedio").value(4.0))
                .andExpect(jsonPath("$.porEstado.CERRADA").value(1));
        consultar("/api/admin/dashboard", admin)
                .andExpect(jsonPath("$.ordenesPorEstado.CERRADA").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.tecnicos[?(@.tecnico.id == " + tecnicoId + ")].resueltas").value(contains(1)));

        // Otro cliente no ve la orden ajena.
        consultar("/api/cliente/ordenes/" + oid + "/historial", registrarCliente()).andExpect(status().isNotFound());
    }

    private String registrarCliente() throws Exception {
        String id = String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000));
        return leer(enviar("/api/auth/registro", null, """
                {"nombres":"Ana","apellidos":"Rojas","dni":"%s","email":"cli-%s@servicestech.test",
                 "telefono":"987654321","clave":"ClaveCliente2026"}""".formatted(id, id))
                .andExpect(status().isCreated()), "$.token");
    }

    private String dniDistinto() {
        return String.format("%08d", (Integer.parseInt(sufijo) + 7) % 100_000_000);
    }
}
