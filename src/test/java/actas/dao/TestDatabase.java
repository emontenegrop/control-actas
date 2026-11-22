package actas.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import actas.db.Jdbc;

/**
 * Base de datos H2 en memoria (modo MySQL) inicializada con los mismos scripts que usa Docker
 * ({@code db/01-schema.sql} y {@code db/02-seed.sql}).
 */
public final class TestDatabase {

    private TestDatabase() {
    }

    public static Jdbc nueva() {
        String url = "jdbc:h2:mem:" + UUID.randomUUID()
                + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USUARIO,VALUE;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url, "sa", "");
             Statement st = conn.createStatement()) {
            for (String script : new String[] {"/db/01-schema.sql", "/db/02-seed.sql"}) {
                for (String sentencia : leer(script).split(";\\s*\\n")) {
                    String sql = sinComentarios(sentencia);
                    // SET NAMES es solo para el cliente mysql de Docker
                    if (!sql.isBlank() && !sql.strip().startsWith("SET NAMES")) {
                        st.execute(sql);
                    }
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo inicializar la base de pruebas", e);
        }
        return new Jdbc(() -> DriverManager.getConnection(url, "sa", ""));
    }

    private static String leer(String recurso) {
        try (InputStream in = TestDatabase.class.getResourceAsStream(recurso)) {
            if (in == null) {
                throw new IllegalStateException("No se encontró " + recurso);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static String sinComentarios(String sql) {
        return sql.lines().filter(l -> !l.strip().startsWith("--")).reduce("", (a, b) -> a + "\n" + b);
    }
}
