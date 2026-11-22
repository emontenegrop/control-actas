package actas.ui;

import static actas.ui.components.ListTableModel.col;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.filechooser.FileNameExtensionFilter;

import actas.AppContext;
import actas.model.Acta;
import actas.model.EstadoReunion;
import actas.model.Reunion;
import actas.service.ActaFormatter;
import actas.ui.components.ListTableModel;

/** Consulta de actas de reuniones cerradas, con exportación a Markdown. */
public class ReunionesCerradasFrame extends javax.swing.JInternalFrame {

    private final AppContext ctx;
    private final ListTableModel<Reunion> modelo = new ListTableModel<>(
            col("Código", Reunion::id),
            col("Fecha", Reunion::fecha),
            col("Comité", Reunion::comite),
            col("Tipo", Reunion::tipoReunion),
            col("Motivo", Reunion::motivo),
            col("Lugar", Reunion::lugar));
    private final JButton btnExportar = new JButton("Exportar acta (Markdown)…");
    private Acta actaActual;

    public ReunionesCerradasFrame(AppContext ctx) {
        this.ctx = ctx;
        initComponents();
        agregarBarraHerramientas();
        tblCerradas.setModel(modelo);
        tblCerradas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblCerradas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                modelo.rowAt(tblCerradas.getSelectedRow()).ifPresent(this::mostrar);
            }
        });
        for (var area : java.util.List.of(txtMotivo, txtAsistencias, txtIntervenciones, txtConclusiones)) {
            area.setLineWrap(true);
            area.setWrapStyleWord(true);
        }
        Ui.run(this, () -> modelo.setRows(ctx.reuniones().findByEstado(EstadoReunion.CERRADA)));
        if (modelo.getRowCount() > 0) {
            tblCerradas.setRowSelectionInterval(0, 0);
        }
    }

    /** Envuelve el layout generado para añadir una barra superior sin tocarlo. */
    private void agregarBarraHerramientas() {
        java.awt.Container original = getContentPane();
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnExportar.setEnabled(false);
        btnExportar.addActionListener(evt -> exportar());
        barra.add(btnExportar);
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.add(barra, BorderLayout.NORTH);
        contenedor.add(original, BorderLayout.CENTER);
        setContentPane(contenedor);
    }

    private void mostrar(Reunion r) {
        Ui.run(this, () -> {
            actaActual = ctx.actas().cargar(r.id());
            txtMotivo.setText(ActaFormatter.encabezado(actaActual.reunion()));
            txtAsistencias.setText(ActaFormatter.asistencia(actaActual));
            txtIntervenciones.setText(ActaFormatter.intervenciones(actaActual));
            txtConclusiones.setText(ActaFormatter.resoluciones(actaActual));
            for (var area : java.util.List.of(txtMotivo, txtAsistencias, txtIntervenciones, txtConclusiones)) {
                area.setCaretPosition(0);
            }
            btnExportar.setEnabled(true);
        });
    }

    private void exportar() {
        if (actaActual == null) {
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Markdown (*.md)", "md"));
        chooser.setSelectedFile(new java.io.File("acta-" + actaActual.reunion().fecha() + "-"
                + actaActual.reunion().id() + ".md"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        Path destino = chooser.getSelectedFile().toPath();
        if (!destino.getFileName().toString().endsWith(".md")) {
            destino = destino.resolveSibling(destino.getFileName() + ".md");
        }
        Path archivo = destino;
        Ui.run(this, () -> {
            try {
                Files.writeString(archivo, ActaFormatter.markdown(actaActual), StandardCharsets.UTF_8);
                Ui.info(this, "Acta exportada en:\n" + archivo.toAbsolutePath());
            } catch (IOException e) {
                throw new IllegalStateException("No se pudo guardar el archivo: " + e.getMessage(), e);
            }
        });
    }

    private void tblCerradasMouseClicked(java.awt.event.MouseEvent evt) {
        // gestionado por el ListSelectionListener
    }

    /** Construye el layout (código heredado del diseñador de NetBeans, sin lógica). */
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblCerradas = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtAsistencias = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtMotivo = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtIntervenciones = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        txtConclusiones = new javax.swing.JTextArea();

        setClosable(true);
        setTitle("Reuniones cerradas (actas)");

        tblCerradas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblCerradasMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblCerradas);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Asistencias", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        txtAsistencias.setColumns(20);
        txtAsistencias.setEditable(false);
        txtAsistencias.setRows(5);
        txtAsistencias.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane2.setViewportView(txtAsistencias);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 203, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Motivo", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        txtMotivo.setColumns(20);
        txtMotivo.setEditable(false);
        txtMotivo.setRows(5);
        txtMotivo.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane3.setViewportView(txtMotivo);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Intervenciones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        txtIntervenciones.setColumns(20);
        txtIntervenciones.setEditable(false);
        txtIntervenciones.setRows(5);
        txtIntervenciones.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane4.setViewportView(txtIntervenciones);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Resoluciones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        txtConclusiones.setColumns(20);
        txtConclusiones.setEditable(false);
        txtConclusiones.setRows(5);
        txtConclusiones.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jScrollPane5.setViewportView(txtConclusiones);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 287, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(jScrollPane5, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1138, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 218, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pack();
    }

    // Componentes Swing (layout heredado del diseñador de NetBeans)
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTable tblCerradas;
    private javax.swing.JTextArea txtAsistencias;
    private javax.swing.JTextArea txtConclusiones;
    private javax.swing.JTextArea txtIntervenciones;
    private javax.swing.JTextArea txtMotivo;
}
