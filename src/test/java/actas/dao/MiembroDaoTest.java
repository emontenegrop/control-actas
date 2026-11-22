package actas.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import actas.db.DataAccessException;
import actas.model.Miembro;

class MiembroDaoTest {

    private MiembroDao dao;

    @BeforeEach
    void setUp() {
        dao = new MiembroDao(TestDatabase.nueva());
    }

    @Test
    void listaLosMiembrosConSuCargoYComite() {
        assertThat(dao.findAll()).hasSize(10);
        Miembro andrea = dao.findById(1).orElseThrow();
        assertThat(andrea.nombre()).isEqualTo("Andrea Salazar");
        assertThat(andrea.cargo()).isEqualTo("Presidente");
        assertThat(andrea.comite()).isEqualTo("Directiva");
    }

    @Test
    void insertaMiembroYSuComiteEnUnaTransaccion() {
        Miembro nuevo = dao.insert(Miembro.nuevo("Ana Prueba", null, null, null, "ana@example.com", 5, 4));

        Miembro leido = dao.findById(nuevo.id()).orElseThrow();
        assertThat(leido.comite()).isEqualTo("Comisión Deportiva");
        assertThat(dao.findByComite(4)).extracting("nombre").containsExactly("Ana Prueba");
    }

    @Test
    void actualizaDatosYCambiaDeComite() {
        Miembro m = dao.findById(8).orElseThrow();
        dao.update(new Miembro(m.id(), "Tomás Ibarra R.", m.cedula(), m.telefono(), m.celular(), m.correo(),
                2, null, 5, null));

        Miembro leido = dao.findById(8).orElseThrow();
        assertThat(leido.nombre()).isEqualTo("Tomás Ibarra R.");
        assertThat(leido.cargo()).isEqualTo("Vicepresidente");
        assertThat(leido.comite()).isEqualTo("Comisión Cultural");
    }

    @Test
    void elNombreSeTrataComoDatoYNoComoSql() {
        // En la versión original este nombre rompía (o inyectaba) la sentencia SQL.
        String malicioso = "O'Brien'); DROP TABLE miembros; --";
        dao.insert(Miembro.nuevo(malicioso, null, null, null, null, 5, 1));

        assertThat(dao.buscarPorNombre("o'brien")).extracting("nombre").containsExactly(malicioso);
        assertThat(dao.findAll()).hasSize(11);
    }

    @Test
    void buscaPorNombreSinDistinguirMayusculas() {
        assertThat(dao.buscarPorNombre("MORENO")).extracting("nombre").containsExactly("Valeria Moreno");
    }

    @Test
    void noPermiteBorrarMiembrosConActasRegistradas() {
        assertThatThrownBy(() -> dao.delete(1))
                .isInstanceOf(DataAccessException.IntegrityViolationException.class);
        assertThat(dao.findById(1)).isPresent();
    }

    @Test
    void borraMiembroSinHistorial() {
        assertThat(dao.delete(10)).isTrue();
        assertThat(dao.findById(10)).isEmpty();
    }
}
