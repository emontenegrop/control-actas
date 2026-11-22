package actas.dao;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import actas.model.AsistenciaResumen;

class ReporteDaoTest {

    @Test
    void calculaElPorcentajeDeAsistenciaPorMiembro() {
        var filas = new ReporteDao(TestDatabase.nueva()).asistenciasPorMiembro();

        assertThat(filas).hasSize(10);
        AsistenciaResumen diego = filas.stream().filter(f -> f.miembro().equals("Diego Paredes")).findFirst().orElseThrow();
        assertThat(diego.convocadas()).isEqualTo(1);
        assertThat(diego.asistidas()).isZero();
        AsistenciaResumen andrea = filas.stream().filter(f -> f.miembro().equals("Andrea Salazar")).findFirst().orElseThrow();
        assertThat(andrea.porcentaje()).isEqualTo(100.0);
    }
}
