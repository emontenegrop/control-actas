package actas.model;

import java.util.List;

/** Vista completa del acta de una reunión cerrada. */
public record Acta(
        Reunion reunion,
        List<Tema> temas,
        List<Convocado> convocados,
        List<String> asistentes,
        List<Intervencion> intervenciones,
        List<Resolucion> resoluciones) {

    public record Convocado(String nombre, String cargo) {
    }

    public record Intervencion(int temaNumero, String tema, String miembro, String texto) {
    }

    public record Resolucion(String tipo, String tema, String texto) {
    }
}
