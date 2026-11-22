package actas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalTime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.api.Test;

import actas.model.Opcion;

class ValidacionesTest {

    @ParameterizedTest
    @ValueSource(strings = {"1722601810", "1729083012", "1709139099", "1708190937"})
    void aceptaCedulasValidas(String cedula) {
        assertThat(Validaciones.esCedulaValida(cedula)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(strings = {"1722601811", "17226018", "abcdefghij", "2522601810", "1772601810", ""})
    void rechazaCedulasInvalidas(String cedula) {
        assertThat(Validaciones.esCedulaValida(cedula)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"9:00, 09:00", "09:30, 09:30", "9h00, 09:00", "14h45, 14:45"})
    void interpretaHorasIncluidoElFormatoHeredado(String texto, LocalTime esperada) {
        assertThat(Validaciones.parseHora(texto)).isEqualTo(esperada);
    }

    @Test
    void validaCorreo() {
        assertThat(Validaciones.esCorreoValido("ana.perez@example.com")).isTrue();
        assertThat(Validaciones.esCorreoValido("ana@")).isFalse();
    }

    @Test
    void reunionAcumulaTodosLosErrores() {
        var form = new ReunionService.Formulario(null, " ", "10/03/2025", "10:00", "09:00", null, null, null);

        assertThatThrownBy(() -> ReunionService.convertir(form))
                .isInstanceOfSatisfying(ValidationException.class, e -> assertThat(e.errores()).hasSize(5));
    }

    @Test
    void reunionRechazaHoraFinAnteriorAlInicio() {
        var tipo = new Opcion(1, "Ordinaria");
        var form = new ReunionService.Formulario(null, "Motivo", "2025-03-10", "10:00", "09:00", null, tipo, tipo);

        assertThatThrownBy(() -> ReunionService.convertir(form))
                .hasMessageContaining("posterior");
    }

    @Test
    void reunionValidaSeConvierte() {
        var tipo = new Opcion(1, "Ordinaria");
        var r = ReunionService.convertir(
                new ReunionService.Formulario(7, " Motivo ", "2025-03-10", "9h00", "", "", tipo, tipo));

        assertThat(r.id()).isEqualTo(7);
        assertThat(r.motivo()).isEqualTo("Motivo");
        assertThat(r.horaInicio()).isEqualTo(LocalTime.of(9, 0));
        assertThat(r.horaFin()).isNull();
        assertThat(r.lugar()).isNull();
    }
}
