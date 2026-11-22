package actas.ui;

import static actas.ui.components.ListTableModel.col;

import java.util.function.Consumer;
import javax.swing.ListSelectionModel;

import actas.AppContext;
import actas.model.EstadoReunion;
import actas.model.Reunion;
import actas.ui.components.ListTableModel;

/**
 * Lista las reuniones programadas para abrir su acta. La reunión elegida se pasa al siguiente
 * formulario como parámetro (antes viajaba en un {@code ArrayList} estático global).
 */
public class SeleccionReunionFrame extends javax.swing.JInternalFrame {

    private final AppContext ctx;
    private final Consumer<Reunion> alSeleccionar;
    private final ListTableModel<Reunion> modelo = new ListTableModel<>(
            col("Fecha", Reunion::fecha),
            col("Hora", Reunion::horaInicio),
            col("Comité", Reunion::comite),
            col("Tipo", Reunion::tipoReunion),
            col("Motivo", Reunion::motivo),
            col("Lugar", Reunion::lugar));

    public SeleccionReunionFrame(AppContext ctx, Consumer<Reunion> alSeleccionar) {
        this.ctx = ctx;
        this.alSeleccionar = alSeleccionar;
        initComponents();
        tblReuniones.setModel(modelo);
        tblReuniones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblReuniones.getSelectionModel().addListSelectionListener(e -> actualizarBoton());
        Ui.run(this, () -> modelo.setRows(ctx.reuniones().findByEstado(EstadoReunion.PROGRAMADA)));
        actualizarBoton();
    }

    private void actualizarBoton() {
        btnIniciar.setEnabled(tblReuniones.getSelectedRow() >= 0);
    }

    private void tblReunionesMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() == 2) {
            btnIniciarActionPerformed(null);
        }
    }

    private void btnIniciarActionPerformed(java.awt.event.ActionEvent evt) {
        modelo.rowAt(tblReuniones.getSelectedRow()).ifPresent(reunion -> {
            alSeleccionar.accept(reunion);
            dispose();
        });
    }

    /** Construye el layout (código heredado del diseñador de NetBeans, sin lógica). */
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        tblReuniones = new javax.swing.JTable();
        btnIniciar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setTitle("Seleccionar reunión");

        tblReuniones.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblReunionesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblReuniones);

        btnIniciar.setText("Abrir acta de la reunión");
        btnIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnIniciarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(107, 107, 107)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 916, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnIniciar))
                .addContainerGap(21, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 352, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnIniciar)
                .addContainerGap(138, Short.MAX_VALUE))
        );

        pack();
    }

    // Componentes Swing (layout heredado del diseñador de NetBeans)
    private javax.swing.JButton btnIniciar;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblReuniones;
}
