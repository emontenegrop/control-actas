package actas.dao;

import java.util.List;

import actas.db.Jdbc;
import actas.model.Acta;
import actas.model.Reunion;

/** Registro (intervenciones, resoluciones) y lectura del acta de una reunión. */
public class ActaDao {

    private final Jdbc jdbc;
    private final ReunionDao reuniones;
    private final TemaDao temas;

    public ActaDao(Jdbc jdbc) {
        this.jdbc = jdbc;
        this.reuniones = new ReunionDao(jdbc);
        this.temas = new TemaDao(jdbc);
    }

    public int agregarIntervencion(int reunionId, int miembroId, int temaId, String texto) {
        return jdbc.insert("""
                INSERT INTO intervenciones (INTERVENCION, REUNIONES_COD_REUNION, MIEMBROS_COD_MIEMBRO, TEMAS_COD_TEMA)
                VALUES (?, ?, ?, ?)
                """, texto, reunionId, miembroId, temaId);
    }

    public int agregarResolucion(int reunionId, int tipoResolucionId, int temaId, String texto) {
        return jdbc.insert("""
                INSERT INTO resoluciones (RESOLUCION, TIPO_RESOLUCION_COD_TIPO_RESOLUCION,
                                          REUNIONES_COD_REUNION, TEMAS_COD_TEMA)
                VALUES (?, ?, ?, ?)
                """, texto, tipoResolucionId, reunionId, temaId);
    }

    /** Carga toda la información del acta. Reemplaza las ~8 consultas concatenadas del original. */
    public Acta cargar(int reunionId) {
        Reunion reunion = reuniones.findById(reunionId)
                .orElseThrow(() -> new IllegalArgumentException("No existe la reunión " + reunionId));

        List<Acta.Convocado> convocados = jdbc.query("""
                SELECT m.NOMBRE, c.CARGO
                  FROM miembros m
                  JOIN cargos c ON c.COD_CARGO = m.CARGOS_COD_CARGO
                  JOIN miembros_comite mc ON mc.MIEMBROS_COD_MIEMBRO = m.COD_MIEMBRO
                 WHERE mc.COMITES_COD_COMITE = ?
                 ORDER BY m.COD_MIEMBRO
                """, rs -> new Acta.Convocado(rs.getString(1), rs.getString(2)), reunion.comiteId());

        List<String> asistentes = jdbc.query("""
                SELECT m.NOMBRE
                  FROM asistencias a
                  JOIN miembros m ON m.COD_MIEMBRO = a.MIEMBROS_COD_MIEMBRO
                 WHERE a.REUNIONES_COD_REUNION = ?
                 ORDER BY m.NOMBRE
                """, rs -> rs.getString(1), reunionId);

        List<Acta.Intervencion> intervenciones = jdbc.query("""
                SELECT t.NUMERO, t.TEMA, m.NOMBRE, i.INTERVENCION
                  FROM intervenciones i
                  JOIN temas t ON t.COD_TEMA = i.TEMAS_COD_TEMA
                  JOIN miembros m ON m.COD_MIEMBRO = i.MIEMBROS_COD_MIEMBRO
                 WHERE i.REUNIONES_COD_REUNION = ?
                 ORDER BY t.NUMERO, i.COD_INTERVENCION
                """, rs -> new Acta.Intervencion(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4)),
                reunionId);

        List<Acta.Resolucion> resoluciones = jdbc.query("""
                SELECT tr.TIPO, t.TEMA, r.RESOLUCION
                  FROM resoluciones r
                  JOIN tipo_resolucion tr ON tr.COD_TIPO_RESOLUCION = r.TIPO_RESOLUCION_COD_TIPO_RESOLUCION
                  JOIN temas t ON t.COD_TEMA = r.TEMAS_COD_TEMA
                 WHERE r.REUNIONES_COD_REUNION = ?
                 ORDER BY tr.TIPO, t.NUMERO, r.COD_RESOLUCION
                """, rs -> new Acta.Resolucion(rs.getString(1), rs.getString(2), rs.getString(3)), reunionId);

        return new Acta(reunion, temas.findByReunion(reunionId), convocados, asistentes, intervenciones, resoluciones);
    }
}
