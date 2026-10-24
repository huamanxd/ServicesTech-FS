package pe.fullstack.servicesbackend.config;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Traduce DATABASE_URL (formato postgres://usuario:clave@host:puerto/base, el que entrega Render)
 * a las propiedades JDBC de Spring. Solo actúa si DB_URL no está definida: DB_URL siempre tiene prioridad.
 */
public final class UrlBaseDatos {

    private UrlBaseDatos() {
    }

    public static void aplicar(Map<String, String> entorno) {
        traducir(entorno).forEach(System::setProperty);
    }

    static Map<String, String> traducir(Map<String, String> entorno) {
        Map<String, String> props = new LinkedHashMap<>();
        String url = entorno.get("DATABASE_URL");
        if (vacio(url) || !vacio(entorno.get("DB_URL"))) return props;

        URI uri = URI.create(url.strip());
        if (!"postgres".equals(uri.getScheme()) && !"postgresql".equals(uri.getScheme())) {
            throw new IllegalStateException("DATABASE_URL debe empezar con postgres:// o postgresql://");
        }
        String puerto = uri.getPort() > 0 ? ":" + uri.getPort() : "";
        String consulta = uri.getRawQuery() == null ? "" : "?" + uri.getRawQuery();
        props.put("spring.datasource.url", "jdbc:postgresql://" + uri.getHost() + puerto + uri.getRawPath() + consulta);

        String info = uri.getRawUserInfo();
        if (info != null) {
            String[] partes = info.split(":", 2);
            if (vacio(entorno.get("DB_USER"))) props.put("spring.datasource.username", decodificar(partes[0]));
            if (partes.length > 1 && vacio(entorno.get("DB_PASSWORD"))) {
                props.put("spring.datasource.password", decodificar(partes[1]));
            }
        }
        return props;
    }

    private static boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private static String decodificar(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
