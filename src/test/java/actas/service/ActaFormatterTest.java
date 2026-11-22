package actas.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import actas.dao.ActaDao;
import actas.dao.TestDatabase;
import actas.model.Acta;

class ActaFormatterTest {

    private final Acta acta = new ActaDao(TestDatabase.nueva()).cargar(1);

    @Test
    void generaMarkdownConTodasLasSecciones() {
        String md = ActaFormatter.markdown(acta);

        assertThat(md)
                .startsWith("# Acta de reunión: Planificación anual de actividades")
                .contains("## Orden del día", "2. Presupuesto del año")
                .contains("## Asistencia (3 de 4)")
                .contains("- [x] Andrea Salazar — Presidente", "- [ ] Diego Paredes — Tesorero")
                .contains("### Administrativa", "Aprobar el presupuesto anual");
    }

    @Test
    void agrupaResolucionesPorTipo() {
        assertThat(ActaFormatter.resoluciones(acta)).startsWith("ADMINISTRATIVA\n• Aprobar");
    }
}
