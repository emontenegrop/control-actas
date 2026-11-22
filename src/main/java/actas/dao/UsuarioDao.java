package actas.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import actas.db.Jdbc;
import actas.model.Usuario;
import actas.security.PasswordHasher;

/** Usuarios del sistema y autenticación. */
public class UsuarioDao {

    private static final String SELECT = """
            SELECT u.COD_USUARIO, u.USUARIO, u.CEDULA, u.TIPO_USUARIO_COD_TIPO_USUARIO, t.TIPO
              FROM usuario u
              JOIN tipo_usuario t ON t.COD_TIPO_USUARIO = u.TIPO_USUARIO_COD_TIPO_USUARIO
            """;

    private final Jdbc jdbc;

    public UsuarioDao(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public List<Usuario> findAll() {
        return jdbc.query(SELECT + " ORDER BY u.USUARIO", UsuarioDao::map);
    }

    public List<Usuario> buscar(String texto, boolean porCedula) {
        String columna = porCedula ? "u.CEDULA" : "LOWER(u.USUARIO)";
        return jdbc.query(SELECT + " WHERE " + columna + " LIKE ? ORDER BY u.USUARIO", UsuarioDao::map,
                "%" + texto.trim().toLowerCase(Locale.ROOT) + "%");
    }

    /**
     * Verifica usuario y contraseña. Antes, el diálogo de inicio de sesión no validaba nada.
     */
    public Optional<Usuario> autenticar(String usuario, char[] clave) {
        Optional<String> hash = jdbc.queryOne("SELECT CONTRASENA_HASH FROM usuario WHERE USUARIO = ?",
                rs -> rs.getString(1), usuario);
        if (hash.isEmpty() || !PasswordHasher.verify(clave, hash.get())) {
            return Optional.empty();
        }
        return jdbc.queryOne(SELECT + " WHERE u.USUARIO = ?", UsuarioDao::map, usuario);
    }

    public Usuario insert(String usuario, String cedula, int tipoUsuarioId, char[] clave) {
        int id = jdbc.insert("""
                INSERT INTO usuario (USUARIO, CONTRASENA_HASH, CEDULA, TIPO_USUARIO_COD_TIPO_USUARIO)
                VALUES (?, ?, ?, ?)
                """, usuario, PasswordHasher.hash(clave), cedula, tipoUsuarioId);
        return jdbc.queryOne(SELECT + " WHERE u.COD_USUARIO = ?", UsuarioDao::map, id).orElseThrow();
    }

    /** Actualiza los datos; la contraseña solo cambia si {@code nuevaClave} no está vacía. */
    public boolean update(int id, String usuario, String cedula, int tipoUsuarioId, char[] nuevaClave) {
        if (nuevaClave != null && nuevaClave.length > 0) {
            return jdbc.update("""
                    UPDATE usuario SET USUARIO = ?, CEDULA = ?, TIPO_USUARIO_COD_TIPO_USUARIO = ?, CONTRASENA_HASH = ?
                     WHERE COD_USUARIO = ?
                    """, usuario, cedula, tipoUsuarioId, PasswordHasher.hash(nuevaClave), id) == 1;
        }
        return jdbc.update("""
                UPDATE usuario SET USUARIO = ?, CEDULA = ?, TIPO_USUARIO_COD_TIPO_USUARIO = ?
                 WHERE COD_USUARIO = ?
                """, usuario, cedula, tipoUsuarioId, id) == 1;
    }

    public boolean delete(int id) {
        return jdbc.update("DELETE FROM usuario WHERE COD_USUARIO = ?", id) == 1;
    }

    private static Usuario map(ResultSet rs) throws SQLException {
        return new Usuario(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getInt(4), rs.getString(5));
    }
}
