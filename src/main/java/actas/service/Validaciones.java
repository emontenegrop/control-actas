package actas.service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Reglas de validación del dominio. En la versión original no existía ninguna validación:
 * cualquier texto se insertaba directamente en el SQL.
 */
public final class Validaciones {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ISO_LOCAL_DATE; // aaaa-mm-dd
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("H:mm");

    private static final Pattern CORREO = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern TELEFONO = Pattern.compile("^[0-9+ ()-]{7,20}$");

    private Validaciones() {
    }

    /**
     * Valida una cédula de identidad ecuatoriana (persona natural): 10 dígitos, código de
     * provincia 01-24 (o 30 para ecuatorianos en el exterior), tercer dígito menor a 6 y dígito
     * verificador según el algoritmo módulo 10.
     */
    public static boolean esCedulaValida(String cedula) {
        if (cedula == null || !cedula.matches("\\d{10}")) {
            return false;
        }
        int provincia = Integer.parseInt(cedula.substring(0, 2));
        if ((provincia < 1 || provincia > 24) && provincia != 30) {
            return false;
        }
        if (cedula.charAt(2) - '0' >= 6) {
            return false;
        }
        int suma = 0;
        for (int i = 0; i < 9; i++) {
            int d = (cedula.charAt(i) - '0') * (i % 2 == 0 ? 2 : 1);
            suma += d > 9 ? d - 9 : d;
        }
        int verificador = (10 - suma % 10) % 10;
        return verificador == cedula.charAt(9) - '0';
    }

    public static boolean esCorreoValido(String correo) {
        return correo != null && CORREO.matcher(correo).matches();
    }

    public static LocalDate parseFecha(String texto) {
        try {
            return LocalDate.parse(texto.trim(), FECHA);
        } catch (DateTimeParseException | NullPointerException e) {
            return null;
        }
    }

    /** Acepta "9:00", "09:00" y también el formato heredado "9h00". */
    public static LocalTime parseHora(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalTime.parse(texto.trim().toLowerCase(java.util.Locale.ROOT).replace('h', ':'), HORA);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Acumula errores para mostrarlos todos juntos al usuario. */
    public static final class Errores {
        private final List<String> mensajes = new ArrayList<>();

        public Errores requerido(String valor, String campo) {
            if (valor == null || valor.isBlank()) {
                mensajes.add(campo + " es obligatorio.");
            }
            return this;
        }

        public Errores maxLongitud(String valor, int max, String campo) {
            if (valor != null && valor.length() > max) {
                mensajes.add(campo + " admite como máximo " + max + " caracteres.");
            }
            return this;
        }

        public Errores si(boolean condicionDeError, String mensaje) {
            if (condicionDeError) {
                mensajes.add(mensaje);
            }
            return this;
        }

        public Errores cedulaOpcional(String cedula) {
            return si(!esVacio(cedula) && !esCedulaValida(cedula.trim()), "La cédula no es válida.");
        }

        public Errores correoOpcional(String correo) {
            return si(!esVacio(correo) && !esCorreoValido(correo.trim()), "El correo no es válido.");
        }

        public Errores telefonoOpcional(String tel, String campo) {
            return si(!esVacio(tel) && !TELEFONO.matcher(tel.trim()).matches(), campo + " no es válido.");
        }

        public void lanzarSiHay() {
            if (!mensajes.isEmpty()) {
                throw new ValidationException(mensajes);
            }
        }
    }

    public static boolean esVacio(String s) {
        return s == null || s.isBlank();
    }

    /** Normaliza campos opcionales: texto vacío se guarda como NULL. */
    public static String nullSiVacio(String s) {
        return esVacio(s) ? null : s.trim();
    }
}
