package actas.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Reunión de un comité.
 *
 * @param tipoReunion nombre del tipo (solo lectura, para mostrar)
 * @param comite      nombre del comité (solo lectura, para mostrar)
 */
public record Reunion(
        Integer id,
        String motivo,
        LocalDate fecha,
        LocalTime horaInicio,
        LocalTime horaFin,
        String lugar,
        EstadoReunion estado,
        Integer periodoId,
        int tipoReunionId,
        String tipoReunion,
        Integer usuarioId,
        int comiteId,
        String comite) {

    public static Reunion nueva(String motivo, LocalDate fecha, LocalTime horaInicio, LocalTime horaFin,
                                String lugar, int tipoReunionId, int comiteId) {
        return new Reunion(null, motivo, fecha, horaInicio, horaFin, lugar, EstadoReunion.PROGRAMADA,
                null, tipoReunionId, null, null, comiteId, null);
    }

    public Reunion withId(int nuevoId) {
        return new Reunion(nuevoId, motivo, fecha, horaInicio, horaFin, lugar, estado, periodoId,
                tipoReunionId, tipoReunion, usuarioId, comiteId, comite);
    }

    public boolean cerrada() {
        return estado == EstadoReunion.CERRADA;
    }
}
