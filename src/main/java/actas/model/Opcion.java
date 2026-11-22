package actas.model;

/** Elemento de catálogo (cargo, comité, tipo de reunión...) para listas desplegables. */
public record Opcion(int id, String nombre) {

    @Override
    public String toString() {
        return nombre;
    }
}
