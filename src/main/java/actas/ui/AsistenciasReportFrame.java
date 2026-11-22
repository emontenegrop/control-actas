package actas.ui;

import static actas.ui.components.ListTableModel.col;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import actas.AppContext;
import actas.model.AsistenciaResumen;
import actas.ui.components.ListTableModel;

/**
 * Reporte de asistencia por miembro. En el original el menú "Reportes › Asistencias" existía
 * pero no tenía implementación.
 */
public class AsistenciasReportFrame extends javax.swing.JInternalFrame {

    public AsistenciasReportFrame(AppContext ctx) {
        super("Reporte de asistencias", true, true, true, true);
        ListTableModel<AsistenciaResumen> modelo = new ListTableModel<>(
                col("Comité", AsistenciaResumen::comite),
                col("Miembro", AsistenciaResumen::miembro),
                col("Reuniones cerradas", AsistenciaResumen::convocadas),
                col("Asistencias", AsistenciaResumen::asistidas),
                col("% asistencia", r -> "%.0f %%".formatted(r.porcentaje())));
        JTable tabla = new JTable(modelo);
        tabla.setAutoCreateRowSorter(true);
        tabla.setFillsViewportHeight(true);

        JLabel nota = new JLabel("Asistencia de cada miembro a las reuniones cerradas de su comité.");
        nota.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        setLayout(new BorderLayout());
        add(nota, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);
        setSize(720, 420);
        Ui.run(this, () -> modelo.setRows(ctx.reportes().asistenciasPorMiembro()));
    }
}
