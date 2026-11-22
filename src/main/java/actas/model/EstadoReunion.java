package actas.model;

/** Ciclo de vida de una reunión: se programa y, al registrar el acta, se cierra. */
public enum EstadoReunion {
    PROGRAMADA("programada"),
    CERRADA("cerrada");

    private final String valorBd;

    EstadoReunion(String valorBd) {
        this.valorBd = valorBd;
    }

    public static EstadoReunion fromBd(String valor) {
        for (EstadoReunion e : values()) {
            if (e.valorBd.equalsIgnoreCase(valor)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de reunión desconocido: " + valor);
    }

    /** Valor almacenado en la columna ESTADO. */
    @Override
    public String toString() {
        return valorBd;
    }
}
