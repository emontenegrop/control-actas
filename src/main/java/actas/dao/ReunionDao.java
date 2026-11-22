package actas.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import actas.db.Jdbc;
import actas.db.Rows;
import actas.model.EstadoReunion;
import actas.model.Reunion;

/** Programación de reuniones y cierre del acta. */
public class ReunionDao {

    private static final String SELECT = """
            SELECT r.COD_REUNION, r.MOTIVO, r.FECHA, r.HORA_INICIO, r.HORA_FIN, r.LUGAR, r.ESTADO,
                   r.PERIODOS_COD_PERIODO, r.TIPO_REUNION_COD_TIPO_REUNION, t.TIPO,
                   r.USUARIO_COD_USUARIO, r.COD_COMITE, c.COMITE
              FROM reuniones r
              JOIN tipo_reunion t ON t.COD_TIPO_REUNION = r.TIPO_REUNION_COD_TIPO_REUNION
              JOIN comites c ON c.COD_COMITE = r.COD_COMITE
            """;
    private static final String ORDER = " ORDER BY r.FECHA DESC, r.HORA_INICIO DESC";

    private final Jdbc jdbc;
    private final CatalogoDao catalogos;

    public ReunionDao(Jdbc jdbc) {
        this.jdbc = jdbc;
        this.catalogos = new CatalogoDao(jdbc);
    }

    public List<Reunion> findAll() {
        return jdbc.query(SELECT + ORDER, ReunionDao::map);
    }

    public List<Reunion> findByEstado(EstadoReunion estado) {
        return jdbc.query(SELECT + " WHERE r.ESTADO = ?" + ORDER, ReunionDao::map, estado);
    }

    public Optional<Reunion> findById(int id) {
        return jdbc.queryOne(SELECT + " WHERE r.COD_REUNION = ?", ReunionDao::map, id);
    }

    public List<Reunion> buscarPorMotivo(String texto) {
        return jdbc.query(SELECT + " WHERE LOWER(r.MOTIVO) LIKE ?" + ORDER, ReunionDao::map,
                "%" + texto.trim().toLowerCase(java.util.Locale.ROOT) + "%");
    }

    /** Programa una reunión en el periodo actual, registrando el usuario que la creó. */
    public Reunion insert(Reunion r, Integer usuarioId) {
        Integer periodo = catalogos.periodoActualId().orElse(null);
        int id = jdbc.insert("""
                INSERT INTO reuniones (MOTIVO, FECHA, HORA_INICIO, HORA_FIN, LUGAR, ESTADO,
                                       PERIODOS_COD_PERIODO, TIPO_REUNION_COD_TIPO_REUNION,
                                       USUARIO_COD_USUARIO, COD_COMITE)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, r.motivo(), r.fecha(), r.horaInicio(), r.horaFin(), r.lugar(), EstadoReunion.PROGRAMADA,
                periodo, r.tipoReunionId(), usuarioId, r.comiteId());
        return findById(id).orElseThrow();
    }

    /** Solo se pueden modificar reuniones que aún no se han cerrado. */
    public boolean update(Reunion r) {
        return jdbc.update("""
                UPDATE reuniones
                   SET MOTIVO = ?, FECHA = ?, HORA_INICIO = ?, HORA_FIN = ?, LUGAR = ?,
                       TIPO_REUNION_COD_TIPO_REUNION = ?, COD_COMITE = ?
                 WHERE COD_REUNION = ? AND ESTADO = ?
                """, r.motivo(), r.fecha(), r.horaInicio(), r.horaFin(), r.lugar(),
                r.tipoReunionId(), r.comiteId(), r.id(), EstadoReunion.PROGRAMADA) == 1;
    }

    /** Borra la reunión; temas, intervenciones, resoluciones y asistencias se eliminan en cascada. */
    public boolean delete(int id) {
        return jdbc.update("DELETE FROM reuniones WHERE COD_REUNION = ?", id) == 1;
    }

    /**
     * Cierra la reunión registrando la asistencia de los miembros presentes. Todo ocurre en una
     * transacción: si algo falla no quedan asistencias a medias ni la reunión cerrada sin ellas.
     *
     * @throws IllegalStateException si la reunión no existe o ya estaba cerrada
     */
    public void cerrar(int reunionId, Collection<Integer> miembrosPresentes) {
        jdbc.inTransaction(tx -> {
            int actualizadas = tx.update("UPDATE reuniones SET ESTADO = ? WHERE COD_REUNION = ? AND ESTADO = ?",
                    EstadoReunion.CERRADA, reunionId, EstadoReunion.PROGRAMADA);
            if (actualizadas != 1) {
                throw new IllegalStateException("La reunión " + reunionId + " no existe o ya está cerrada");
            }
            for (int miembroId : miembrosPresentes) {
                tx.update("""
                        INSERT INTO asistencias (PERIODOS_COD_PERIODO, MIEMBROS_COD_MIEMBRO,
                                                 COMITES_COD_COMITE, REUNIONES_COD_REUNION)
                        SELECT r.PERIODOS_COD_PERIODO, ?, r.COD_COMITE, r.COD_REUNION
                          FROM reuniones r WHERE r.COD_REUNION = ?
                        """, miembroId, reunionId);
            }
            return null;
        });
    }

    private static Reunion map(ResultSet rs) throws SQLException {
        return new Reunion(
                rs.getInt("COD_REUNION"),
                rs.getString("MOTIVO"),
                Rows.date(rs, "FECHA"),
                Rows.time(rs, "HORA_INICIO"),
                Rows.time(rs, "HORA_FIN"),
                rs.getString("LUGAR"),
                EstadoReunion.fromBd(rs.getString("ESTADO")),
                Rows.nullableInt(rs, "PERIODOS_COD_PERIODO"),
                rs.getInt("TIPO_REUNION_COD_TIPO_REUNION"),
                rs.getString("TIPO"),
                Rows.nullableInt(rs, "USUARIO_COD_USUARIO"),
                rs.getInt("COD_COMITE"),
                rs.getString("COMITE"));
    }
}
