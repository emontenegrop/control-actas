package actas.service;

import java.time.LocalDate;
import java.time.LocalTime;

import actas.dao.ReunionDao;
import actas.model.Opcion;
import actas.model.Reunion;

/** Convierte y valida los datos del formulario de reuniones. */
public class ReunionService {

    private final ReunionDao dao;

    public ReunionService(ReunionDao dao) {
        this.dao = dao;
    }

    /** Datos tal como llegan del formulario (texto libre). */
    public record Formulario(Integer id, String motivo, String fecha, String horaInicio, String horaFin,
                             String lugar, Opcion tipo, Opcion comite) {
    }

    public Reunion guardar(Formulario f, Integer usuarioId) {
        Reunion r = convertir(f);
        if (r.id() == null) {
            return dao.insert(r, usuarioId);
        }
        if (!dao.update(r)) {
            throw new IllegalStateException("La reunión ya fue cerrada y no puede modificarse.");
        }
        return r;
    }

    static Reunion convertir(Formulario f) {
        LocalDate fecha = Validaciones.parseFecha(f.fecha());
        LocalTime inicio = Validaciones.parseHora(f.horaInicio());
        LocalTime fin = Validaciones.parseHora(f.horaFin());
        new Validaciones.Errores()
                .requerido(f.motivo(), "El motivo")
                .maxLongitud(f.motivo(), 200, "El motivo")
                .si(fecha == null, "La fecha debe tener el formato aaaa-mm-dd.")
                .si(inicio == null, "La hora de inicio debe tener el formato HH:mm.")
                .si(!Validaciones.esVacio(f.horaFin()) && fin == null, "La hora de fin debe tener el formato HH:mm.")
                .si(inicio != null && fin != null && !fin.isAfter(inicio), "La hora de fin debe ser posterior a la de inicio.")
                .maxLongitud(f.lugar(), 150, "El lugar")
                .si(f.tipo() == null, "Seleccione el tipo de reunión.")
                .si(f.comite() == null, "Seleccione el comité.")
                .lanzarSiHay();
        Reunion nueva = Reunion.nueva(f.motivo().trim(), fecha, inicio, fin, Validaciones.nullSiVacio(f.lugar()),
                f.tipo().id(), f.comite().id());
        return f.id() == null ? nueva : nueva.withId(f.id());
    }
}
