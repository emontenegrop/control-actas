package actas.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DatabaseConfigTest {

    @TempDir
    Path dir;

    @Test
    void usaValoresPorDefecto() {
        DatabaseConfig c = DatabaseConfig.load(dir.resolve("no-existe.properties"), Map.of());
        assertThat(c.url()).startsWith("jdbc:mysql://localhost:3306/actas");
        assertThat(c.user()).isEqualTo("actas");
    }

    @Test
    void archivoExternoYVariablesDeEntornoSobrescriben() throws Exception {
        Path f = dir.resolve("actas.properties");
        Files.writeString(f, "db.url=jdbc:mysql://servidor/actas\ndb.user=desde-archivo\n");

        DatabaseConfig c = DatabaseConfig.load(f, Map.of("ACTAS_DB_USER", "desde-env", "ACTAS_DB_PASSWORD", "x"));

        assertThat(c.url()).isEqualTo("jdbc:mysql://servidor/actas");
        assertThat(c.user()).isEqualTo("desde-env");
        assertThat(c.toString()).doesNotContain("x]").contains("****");
    }
}
