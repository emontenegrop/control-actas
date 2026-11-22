package actas.dao;

import java.util.List;
import java.util.Optional;

import actas.db.Jdbc;
import actas.model.Opcion;

/** Catálogos usados en las listas desplegables. */
public class CatalogoDao {

    private final Jdbc jdbc;

    public CatalogoDao(Jdbc jdbc) {
        this.jdbc = jdbc;
    }

    public List<Opcion> cargos() {
        return opciones("SELECT COD_CARGO, CARGO FROM cargos ORDER BY COD_CARGO");
    }

    public List<Opcion> comites() {
        return opciones("SELECT COD_COMITE, COMITE FROM comites ORDER BY COMITE");
    }

    public List<Opcion> tiposReunion() {
        return opciones("SELECT COD_TIPO_REUNION, TIPO FROM tipo_reunion ORDER BY COD_TIPO_REUNION");
    }

    public List<Opcion> tiposResolucion() {
        return opciones("SELECT COD_TIPO_RESOLUCION, TIPO FROM tipo_resolucion ORDER BY TIPO");
    }

    public List<Opcion> tiposUsuario() {
        return opciones("SELECT COD_TIPO_USUARIO, TIPO FROM tipo_usuario ORDER BY COD_TIPO_USUARIO");
    }

    /** Periodo marcado como actual (antes estaba fijo en el código como "2"). */
    public Optional<Integer> periodoActualId() {
        return jdbc.queryOne("SELECT COD_PERIODO FROM periodos WHERE ES_ACTUAL = TRUE ORDER BY INICIO DESC",
                rs -> rs.getInt(1));
    }

    private List<Opcion> opciones(String sql) {
        return jdbc.query(sql, rs -> new Opcion(rs.getInt(1), rs.getString(2)));
    }
}
