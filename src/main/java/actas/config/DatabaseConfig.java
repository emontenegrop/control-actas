package actas.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Parámetros de conexión a la base de datos.
 *
 * <p>Orden de precedencia (el último gana):
 * <ol>
 *   <li>{@code actas.properties} empaquetado en el JAR (valores por defecto).</li>
 *   <li>{@code actas.properties} en el directorio de trabajo, si existe.</li>
 *   <li>Variables de entorno {@code ACTAS_DB_URL}, {@code ACTAS_DB_USER}, {@code ACTAS_DB_PASSWORD}.</li>
 * </ol>
 * En el código original la URL, el usuario {@code root} y la clave vacía estaban fijos en el código.
 */
public record DatabaseConfig(String url, String user, String password) {

    public static final String FILE_NAME = "actas.properties";

    public DatabaseConfig {
        Objects.requireNonNull(url, "db.url es obligatorio");
        user = user == null ? "" : user;
        password = password == null ? "" : password;
    }

    public static DatabaseConfig load() {
        return load(Path.of(FILE_NAME), System.getenv());
    }

    static DatabaseConfig load(Path externalFile, Map<String, String> env) {
        Properties props = new Properties();
        try (InputStream in = DatabaseConfig.class.getResourceAsStream("/" + FILE_NAME)) {
            if (in != null) {
                props.load(new java.io.InputStreamReader(in, StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la configuración por defecto", e);
        }
        if (Files.isRegularFile(externalFile)) {
            try (Reader reader = Files.newBufferedReader(externalFile, StandardCharsets.UTF_8)) {
                props.load(reader);
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo leer " + externalFile.toAbsolutePath(), e);
            }
        }
        return new DatabaseConfig(
                env.getOrDefault("ACTAS_DB_URL", props.getProperty("db.url")),
                env.getOrDefault("ACTAS_DB_USER", props.getProperty("db.user")),
                env.getOrDefault("ACTAS_DB_PASSWORD", props.getProperty("db.password")));
    }

    /** Evita filtrar la contraseña en logs. */
    @Override
    public String toString() {
        return "DatabaseConfig[url=" + url + ", user=" + user + ", password=****]";
    }
}
