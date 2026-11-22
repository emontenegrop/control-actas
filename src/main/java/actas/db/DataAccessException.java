package actas.db;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

/** Excepción no comprobada para errores de acceso a datos. */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    static DataAccessException translate(SQLException e, String sql) {
        if (isConstraintViolation(e)) {
            return new IntegrityViolationException(e);
        }
        return new DataAccessException("Error de base de datos ejecutando: " + sql, e);
    }

    /** SQLState clase 23 = violación de integridad (FK, UNIQUE, NOT NULL, CHECK). */
    private static boolean isConstraintViolation(SQLException e) {
        return e instanceof SQLIntegrityConstraintViolationException
                || (e.getSQLState() != null && e.getSQLState().startsWith("23"));
    }

    /** Violación de una restricción: registro duplicado o referenciado por otros datos. */
    public static class IntegrityViolationException extends DataAccessException {
        IntegrityViolationException(SQLException cause) {
            super("La operación viola una restricción de integridad de los datos", cause);
        }
    }
}
