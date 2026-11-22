package actas.model;

/** Fila del reporte de asistencias por miembro. */
public record AsistenciaResumen(String miembro, String comite, int convocadas, int asistidas) {

    public double porcentaje() {
        return convocadas == 0 ? 0 : (asistidas * 100.0) / convocadas;
    }
}
