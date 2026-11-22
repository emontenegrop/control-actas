package actas.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import actas.model.Acta;
import actas.model.Reunion;

/** Genera el texto del acta para la pantalla y para exportar en Markdown. */
public final class ActaFormatter {

    private ActaFormatter() {
    }

    public static String encabezado(Reunion r) {
        StringBuilder sb = new StringBuilder()
                .append("Fecha: ").append(r.fecha()).append('\n')
                .append("Hora de inicio: ").append(r.horaInicio());
        if (r.horaFin() != null) {
            sb.append("   Hora de fin: ").append(r.horaFin());
        }
        sb.append('\n');
        if (r.lugar() != null) {
            sb.append("Lugar: ").append(r.lugar()).append('\n');
        }
        return sb.append("Comité: ").append(r.comite()).append(" (").append(r.tipoReunion()).append(")\n\n")
                .append(r.motivo())
                .toString();
    }

    public static String asistencia(Acta acta) {
        StringBuilder sb = new StringBuilder("CONVOCADOS\n");
        acta.convocados().forEach(c -> sb.append(c.cargo()).append(": ").append(c.nombre()).append('\n'));
        sb.append("\nASISTENTES (").append(acta.asistentes().size()).append('/')
                .append(acta.convocados().size()).append(")\n");
        acta.asistentes().forEach(a -> sb.append(a).append('\n'));
        return sb.toString();
    }

    public static String intervenciones(Acta acta) {
        return acta.intervenciones().stream()
                .map(i -> i.temaNumero() + ". " + i.tema() + "  —  " + i.miembro() + "\n" + i.texto())
                .collect(Collectors.joining("\n\n"));
    }

    public static String resoluciones(Acta acta) {
        StringBuilder sb = new StringBuilder();
        porTipo(acta.resoluciones()).forEach((tipo, lista) -> {
            sb.append(tipo.toUpperCase()).append('\n');
            lista.forEach(r -> sb.append("• ").append(r.texto()).append('\n'));
            sb.append('\n');
        });
        return sb.toString().strip();
    }

    public static String markdown(Acta acta) {
        Reunion r = acta.reunion();
        StringBuilder md = new StringBuilder()
                .append("# Acta de reunión: ").append(r.motivo()).append("\n\n")
                .append("| | |\n|---|---|\n")
                .append("| **Comité** | ").append(r.comite()).append(" |\n")
                .append("| **Tipo** | ").append(r.tipoReunion()).append(" |\n")
                .append("| **Fecha** | ").append(r.fecha()).append(" |\n")
                .append("| **Horario** | ").append(r.horaInicio())
                .append(r.horaFin() != null ? " – " + r.horaFin() : "").append(" |\n")
                .append("| **Lugar** | ").append(r.lugar() == null ? "—" : r.lugar()).append(" |\n\n");

        md.append("## Orden del día\n\n");
        acta.temas().forEach(t -> md.append(t.numero()).append(". ").append(t.tema()).append('\n'));

        md.append("\n## Asistencia (").append(acta.asistentes().size()).append(" de ")
                .append(acta.convocados().size()).append(")\n\n");
        acta.convocados().forEach(c -> md.append("- [").append(acta.asistentes().contains(c.nombre()) ? "x" : " ")
                .append("] ").append(c.nombre()).append(" — ").append(c.cargo()).append('\n'));

        md.append("\n## Intervenciones\n\n");
        if (acta.intervenciones().isEmpty()) {
            md.append("_Sin intervenciones registradas._\n");
        }
        acta.intervenciones().forEach(i -> md.append("- **").append(i.miembro()).append("** (tema ")
                .append(i.temaNumero()).append("): ").append(i.texto()).append('\n'));

        md.append("\n## Resoluciones\n");
        if (acta.resoluciones().isEmpty()) {
            md.append("\n_Sin resoluciones registradas._\n");
        }
        porTipo(acta.resoluciones()).forEach((tipo, lista) -> {
            md.append("\n### ").append(tipo).append("\n\n");
            lista.forEach(res -> md.append("- ").append(res.texto()).append(" _(").append(res.tema()).append(")_\n"));
        });
        return md.toString();
    }

    private static Map<String, List<Acta.Resolucion>> porTipo(List<Acta.Resolucion> resoluciones) {
        return resoluciones.stream()
                .collect(Collectors.groupingBy(Acta.Resolucion::tipo, LinkedHashMap::new, Collectors.toList()));
    }
}
