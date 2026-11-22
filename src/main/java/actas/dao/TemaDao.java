package actas.dao;

import java.util.List;

import actas.db.Jdbc;
import actas.model.Tema;

/** Orden del día (temas) de cada reunión. */
public class TemaDao {

    private final Jdbc jdbc;

    public TemaDao(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public List<Tema> findByReunion(int reunionId) {
        return jdbc.query("""
                SELECT COD_TEMA, NUMERO, TEMA, REUNIONES_COD_REUNION
                  FROM temas WHERE REUNIONES_COD_REUNION = ? ORDER BY NUMERO
                """, rs -> new Tema(rs.getInt(1), rs.getInt(2), rs.getString(3), rs.getInt(4)), reunionId);
    }

    /** Agrega un tema al final del orden del día (número = máximo actual + 1). */
    public Tema agregar(int reunionId, String texto) {
        return jdbc.inTransaction(tx -> {
            int numero = tx.queryOne("SELECT COALESCE(MAX(NUMERO), 0) + 1 FROM temas WHERE REUNIONES_COD_REUNION = ?",
                    rs -> rs.getInt(1), reunionId).orElse(1);
            int id = tx.insert("INSERT INTO temas (NUMERO, TEMA, REUNIONES_COD_REUNION) VALUES (?, ?, ?)",
                    numero, texto, reunionId);
            return new Tema(id, numero, texto, reunionId);
        });
    }

    public boolean actualizar(int temaId, String texto) {
        return jdbc.update("UPDATE temas SET TEMA = ? WHERE COD_TEMA = ?", texto, temaId) == 1;
    }
}
