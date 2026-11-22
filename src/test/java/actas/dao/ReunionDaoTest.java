package actas.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import actas.db.DataAccessException;
import actas.db.Jdbc;
import actas.model.Acta;
import actas.model.EstadoReunion;
import actas.model.Reunion;

class ReunionDaoTest {

    private Jdbc jdbc;
    private ReunionDao reuniones;
    private TemaDao temas;
    private ActaDao actas;

    @BeforeEach
    void setUp() {
        jdbc = TestDatabase.nueva();
        reuniones = new ReunionDao(jdbc);
        temas = new TemaDao(jdbc);
        actas = new ActaDao(jdbc);
    }

    @Test
    void filtraPorEstado() {
        assertThat(reuniones.findByEstado(EstadoReunion.CERRADA)).hasSize(2);
        assertThat(reuniones.findByEstado(EstadoReunion.PROGRAMADA))
                .singleElement().extracting(Reunion::motivo).isEqualTo("Organización de la feria de integración");
    }

    @Test
    void programaReunionEnElPeriodoActualConElUsuario() {
        Reunion r = reuniones.insert(Reunion.nueva("Reunión de prueba", LocalDate.of(2025, 6, 1),
                LocalTime.of(9, 0), LocalTime.of(10, 15), "Sala B", 2, 3), 1);

        assertThat(r.id()).isPositive();
        assertThat(r.estado()).isEqualTo(EstadoReunion.PROGRAMADA);
        assertThat(r.periodoId()).isEqualTo(1);
        assertThat(r.usuarioId()).isEqualTo(1);
        assertThat(r.horaFin()).isEqualTo(LocalTime.of(10, 15));
        assertThat(r.comite()).isEqualTo("Comisión Social");
    }

    @Test
    void numeraLosTemasConsecutivamente() {
        assertThat(temas.agregar(3, "Varios").numero()).isEqualTo(3);
        Reunion nueva = reuniones.insert(Reunion.nueva("Sin temas", LocalDate.now(), LocalTime.NOON, null,
                null, 1, 1), null);
        assertThat(temas.agregar(nueva.id(), "Primero").numero()).isEqualTo(1);
    }

    @Test
    void noModificaReunionesCerradas() {
        Reunion cerrada = reuniones.findById(1).orElseThrow();
        assertThat(reuniones.update(cerrada)).isFalse();
    }

    @Test
    void cierraLaReunionYRegistraAsistenciasEnUnaTransaccion() {
        int tema = temas.findByReunion(3).getFirst().id();
        actas.agregarIntervencion(3, 1, tema, "Propone realizar la feria el 15 de junio.");
        actas.agregarResolucion(3, 2, tema, "La feria se realizará el 15 de junio.");

        reuniones.cerrar(3, List.of(1, 2, 4));

        Acta acta = actas.cargar(3);
        assertThat(acta.reunion().estado()).isEqualTo(EstadoReunion.CERRADA);
        assertThat(acta.convocados()).hasSize(4);
        assertThat(acta.asistentes()).containsExactly("Andrea Salazar", "Diego Paredes", "Mateo Cevallos");
        assertThat(acta.intervenciones()).singleElement().extracting(Acta.Intervencion::miembro)
                .isEqualTo("Andrea Salazar");
        assertThat(acta.resoluciones()).singleElement().extracting(Acta.Resolucion::tipo)
                .isEqualTo("Administrativa");
    }

    @Test
    void noSePuedeCerrarDosVeces() {
        assertThatThrownBy(() -> reuniones.cerrar(1, List.of(1)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void siFallaUnaAsistenciaSeRevierteTodoElCierre() {
        // El miembro 999 no existe: la FK falla y la transacción debe deshacer el UPDATE de estado.
        assertThatThrownBy(() -> reuniones.cerrar(3, List.of(1, 999)))
                .isInstanceOf(DataAccessException.class);

        assertThat(reuniones.findById(3).orElseThrow().estado()).isEqualTo(EstadoReunion.PROGRAMADA);
        assertThat(actas.cargar(3).asistentes()).isEmpty();
    }

    @Test
    void borrarUnaReunionEliminaSuActaEnCascada() {
        assertThat(reuniones.delete(1)).isTrue();
        assertThat(temas.findByReunion(1)).isEmpty();
        assertThat(jdbc.queryOne("SELECT COUNT(*) FROM asistencias WHERE REUNIONES_COD_REUNION = 1",
                rs -> rs.getInt(1))).contains(0);
    }
}
