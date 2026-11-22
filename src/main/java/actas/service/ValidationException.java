package actas.service;

import java.util.List;

/** Errores de validación de datos ingresados por el usuario. */
public class ValidationException extends RuntimeException {

    private final List<String> errores;

    public ValidationException(List<String> errores) {
        super(String.join("\n", errores));
        this.errores = List.copyOf(errores);
    }

    public List<String> errores() {
        return errores;
    }
}
