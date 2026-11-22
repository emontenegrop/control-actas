package actas.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import actas.db.Jdbc;
import actas.db.Rows;
import actas.model.Miembro;
import actas.model.Opcion;

/** CRUD de miembros y su pertenencia a comités. */
public class MiembroDao {

    private static final String SELECT = """
            SELECT m.COD_MIEMBRO, m.NOMBRE, m.CEDULA, m.TELEFONO, m.CELULAR, m.CORREO,
                   m.CARGOS_COD_CARGO, c.CARGO, mc.COMITES_COD_COMITE, co.COMITE
              FROM miembros m
              JOIN cargos c ON c.COD_CARGO = m.CARGOS_COD_CARGO
              LEFT JOIN miembros_comite mc ON mc.MIEMBROS_COD_MIEMBRO = m.COD_MIEMBRO
              LEFT JOIN comites co ON co.COD_COMITE = mc.COMITES_COD_COMITE
            """;

    private final Jdbc jdbc;

    public MiembroDao(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public List<Miembro> findAll() {
        return jdbc.query(SELECT + " ORDER BY m.COD_MIEMBRO", MiembroDao::map);
    }

    public Optional<Miembro> findById(int id) {
        return jdbc.queryOne(SELECT + " WHERE m.COD_MIEMBRO = ?", MiembroDao::map, id);
    }

    public List<Miembro> buscarPorNombre(String texto) {
        String patron = "%" + texto.trim().toLowerCase(Locale.ROOT) + "%";
        return jdbc.query(SELECT + " WHERE LOWER(m.NOMBRE) LIKE ? ORDER BY m.NOMBRE", MiembroDao::map, patron);
    }

    /** Miembros de un comité (convocados a sus reuniones). */
    public List<Opcion> findByComite(int comiteId) {
        return jdbc.query("""
                SELECT m.COD_MIEMBRO, m.NOMBRE
                  FROM miembros m
                  JOIN miembros_comite mc ON mc.MIEMBROS_COD_MIEMBRO = m.COD_MIEMBRO
                 WHERE mc.COMITES_COD_COMITE = ?
                 ORDER BY m.COD_MIEMBRO
                """, rs -> new Opcion(rs.getInt(1), rs.getString(2)), comiteId);
    }

    /** Inserta el miembro y su comité en una sola transacción. */
    public Miembro insert(Miembro m) {
        return jdbc.inTransaction(tx -> {
            int id = tx.insert("""
                    INSERT INTO miembros (NOMBRE, CEDULA, TELEFONO, CELULAR, CORREO, CARGOS_COD_CARGO)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """, m.nombre(), m.cedula(), m.telefono(), m.celular(), m.correo(), m.cargoId());
            asignarComite(tx, id, m.comiteId());
            return m.withId(id);
        });
    }

    public void update(Miembro m) {
        if (m.id() == null) {
            throw new IllegalArgumentException("El miembro no tiene id");
        }
        jdbc.inTransaction(tx -> {
            tx.update("""
                    UPDATE miembros
                       SET NOMBRE = ?, CEDULA = ?, TELEFONO = ?, CELULAR = ?, CORREO = ?, CARGOS_COD_CARGO = ?
                     WHERE COD_MIEMBRO = ?
                    """, m.nombre(), m.cedula(), m.telefono(), m.celular(), m.correo(), m.cargoId(), m.id());
            asignarComite(tx, m.id(), m.comiteId());
            return null;
        });
    }

    /**
     * Elimina un miembro (su pertenencia a comités se borra en cascada). Si tiene asistencias o
     * intervenciones registradas la base de datos lo impide para no romper actas históricas.
     */
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM miembros WHERE COD_MIEMBRO = ?", id) == 1;
    }

    private static void asignarComite(Jdbc tx, int miembroId, Integer comiteId) {
        tx.update("DELETE FROM miembros_comite WHERE MIEMBROS_COD_MIEMBRO = ?", miembroId);
        if (comiteId != null) {
            tx.update("INSERT INTO miembros_comite (COMITES_COD_COMITE, MIEMBROS_COD_MIEMBRO) VALUES (?, ?)",
                    comiteId, miembroId);
        }
    }

    private static Miembro map(ResultSet rs) throws SQLException {
        return new Miembro(
                rs.getInt("COD_MIEMBRO"),
                rs.getString("NOMBRE"),
                rs.getString("CEDULA"),
                rs.getString("TELEFONO"),
                rs.getString("CELULAR"),
                rs.getString("CORREO"),
                rs.getInt("CARGOS_COD_CARGO"),
                rs.getString("CARGO"),
                Rows.nullableInt(rs, "COMITES_COD_COMITE"),
                rs.getString("COMITE"));
    }
}
