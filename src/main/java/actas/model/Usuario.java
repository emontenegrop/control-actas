package actas.model;

/** Usuario del sistema (la contraseña nunca sale de la capa de datos). */
public record Usuario(Integer id, String usuario, String cedula, int tipoUsuarioId, String tipoUsuario) {
}
