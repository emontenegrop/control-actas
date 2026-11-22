package actas.dao;

import java.util.List;

import actas.db.Jdbc;
import actas.model.AsistenciaResumen;

/** Consultas agregadas para reportes. */
public class ReporteDao {

    private final Jdbc jdbc;

    public ReporteDao(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    /** Asistencia de cada miembro a las reuniones cerradas de su comité. */
    public List<AsistenciaResumen> asistenciasPorMiembro() {
        return jdbc.query("""
                SELECT m.NOMBRE, co.COMITE,
                       (SELECT COUNT(*) FROM reuniones r
                         WHERE r.COD_COMITE = co.COD_COMITE AND r.ESTADO = 'cerrada') AS CONVOCADAS,
                       (SELECT COUNT(*) FROM asistencias a
                          JOIN reuniones r ON r.COD_REUNION = a.REUNIONES_COD_REUNION
                         WHERE a.MIEMBROS_COD_MIEMBRO = m.COD_MIEMBRO
                           AND r.COD_COMITE = co.COD_COMITE) AS ASISTIDAS
                  FROM miembros m
                  JOIN miembros_comite mc ON mc.MIEMBROS_COD_MIEMBRO = m.COD_MIEMBRO
                  JOIN comites co ON co.COD_COMITE = mc.COMITES_COD_COMITE
                 ORDER BY co.COMITE, m.NOMBRE
                """, rs -> new AsistenciaResumen(rs.getString(1), rs.getString(2), rs.getInt(3), rs.getInt(4)));
    }
}
