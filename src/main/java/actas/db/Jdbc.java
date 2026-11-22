package actas.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import actas.config.DatabaseConfig;

/**
 * Pequeña capa sobre JDBC (al estilo de {@code JdbcTemplate}) que centraliza:
 * <ul>
 *   <li>Consultas siempre parametrizadas con {@link PreparedStatement} (sin concatenar SQL).</li>
 *   <li>Cierre de recursos con try-with-resources.</li>
 *   <li>Traducción de {@link SQLException} a {@link DataAccessException}.</li>
 *   <li>Transacciones explícitas mediante {@link #inTransaction(TransactionWork)}.</li>
 * </ul>
 * Reemplaza a la clase {@code AccesoDatos} original, que abría una conexión nueva por consulta,
 * nunca la cerraba y silenciaba los errores.
 */
public final class Jdbc {

    @FunctionalInterface
    public interface ConnectionSource {
        Connection get() throws SQLException;
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionWork<T> {
        T execute(Jdbc tx);
    }

    private final ConnectionSource source;
    /** {@code null} fuera de una transacción; en una transacción, la conexión compartida. */
    private final Connection txConnection;

    public Jdbc(ConnectionSource source) {
        this(source, null);
    }

    private Jdbc(ConnectionSource source, Connection txConnection) {
        this.source = source;
        this.txConnection = txConnection;
    }

    public static Jdbc fromConfig(DatabaseConfig config) {
        return new Jdbc(() -> DriverManager.getConnection(config.url(), config.user(), config.password()));
    }

    /** Verifica que la base de datos responde. */
    public void ping() {
        queryOne("SELECT 1", rs -> rs.getInt(1));
    }

    public <T> List<T> query(String sql, RowMapper<T> mapper, Object... params) {
        return withConnection(conn -> {
            try (PreparedStatement ps = prepare(conn, sql, params, false);
                 ResultSet rs = ps.executeQuery()) {
                List<T> rows = new ArrayList<>();
                while (rs.next()) {
                    rows.add(mapper.map(rs));
                }
                return rows;
            }
        }, sql);
    }

    public <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) {
        List<T> rows = query(sql, mapper, params);
        return rows.isEmpty() ? Optional.empty() : Optional.ofNullable(rows.getFirst());
    }

    /** Ejecuta INSERT/UPDATE/DELETE y devuelve las filas afectadas. */
    public int update(String sql, Object... params) {
        return withConnection(conn -> {
            try (PreparedStatement ps = prepare(conn, sql, params, false)) {
                return ps.executeUpdate();
            }
        }, sql);
    }

    /** Ejecuta un INSERT y devuelve la clave autogenerada. */
    public int insert(String sql, Object... params) {
        return withConnection(conn -> {
            try (PreparedStatement ps = prepare(conn, sql, params, true)) {
                ps.executeUpdate();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("El INSERT no devolvió clave generada");
                    }
                    return keys.getInt(1);
                }
            }
        }, sql);
    }

    /**
     * Ejecuta {@code work} en una única transacción: commit si termina bien, rollback ante
     * cualquier excepción. Las transacciones anidadas reutilizan la transacción externa.
     */
    public <T> T inTransaction(TransactionWork<T> work) {
        if (txConnection != null) {
            return work.execute(this);
        }
        try (Connection conn = source.get()) {
            conn.setAutoCommit(false);
            try {
                T result = work.execute(new Jdbc(source, conn));
                conn.commit();
                return result;
            } catch (RuntimeException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error en la transacción", e);
        }
    }

    // ------------------------------------------------------------------ helpers

    @FunctionalInterface
    private interface SqlWork<T> {
        T run(Connection conn) throws SQLException;
    }

    private <T> T withConnection(SqlWork<T> work, String sql) {
        try {
            if (txConnection != null) {
                return work.run(txConnection);
            }
            try (Connection conn = source.get()) {
                return work.run(conn);
            }
        } catch (SQLException e) {
            throw DataAccessException.translate(e, sql);
        }
    }

    private static PreparedStatement prepare(Connection conn, String sql, Object[] params, boolean keys)
            throws SQLException {
        PreparedStatement ps = keys
                ? conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
                : conn.prepareStatement(sql);
        try {
            for (int i = 0; i < params.length; i++) {
                // java.time (LocalDate/LocalTime) se envía directamente con setObject (JDBC 4.2+);
                // los enums se guardan con su representación toString().
                Object p = params[i];
                ps.setObject(i + 1, p instanceof Enum<?> e ? e.toString() : p);
            }
            return ps;
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
    }
}
