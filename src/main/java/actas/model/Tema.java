package actas.model;

/** Punto del orden del día de una reunión. */
public record Tema(int id, int numero, String tema, int reunionId) {

    @Override
    public String toString() {
        return numero + ". " + tema;
    }
}
