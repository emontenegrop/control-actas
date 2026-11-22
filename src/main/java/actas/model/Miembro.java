package actas.model;

/**
 * Miembro de un comité.
 *
 * @param id       {@code null} si aún no se ha guardado
 * @param cargo    nombre del cargo (solo lectura, para mostrar)
 * @param comiteId comité al que pertenece ({@code null} si no tiene)
 * @param comite   nombre del comité (solo lectura, para mostrar)
 */
public record Miembro(
        Integer id,
        String nombre,
        String cedula,
        String telefono,
        String celular,
        String correo,
        int cargoId,
        String cargo,
        Integer comiteId,
        String comite) {

    public static Miembro nuevo(String nombre, String cedula, String telefono, String celular,
                                String correo, int cargoId, Integer comiteId) {
        return new Miembro(null, nombre, cedula, telefono, celular, correo, cargoId, null, comiteId, null);
    }

    public Miembro withId(int nuevoId) {
        return new Miembro(nuevoId, nombre, cedula, telefono, celular, correo, cargoId, cargo, comiteId, comite);
    }
}
