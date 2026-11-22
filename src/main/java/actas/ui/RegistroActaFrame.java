package actas.ui;

import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;

import actas.AppContext;
import actas.model.Opcion;
import actas.model.Reunion;
import actas.model.Tema;
import actas.ui.components.CheckListItem;
import actas.ui.components.CheckListRenderer;

/**
 * Registro del acta de una reunión programada: asistencia, intervenciones por tema y
 * resoluciones. Al cerrar la reunión se guardan las asistencias en una transacción.
 */
public class RegistroActaFrame extends javax.swing.JInternalFrame {

    private final AppContext ctx;
    private final Reunion reunion;
    private final DefaultListModel<CheckListItem<Opcion>> convocados = new DefaultListModel<>();
    private int intervencionesRegistradas;
    private int resolucionesRegistradas;

    public RegistroActaFrame(AppContext ctx, Reunion reunion) {
        this.ctx = ctx;
        this.reunion = reunion;
        initComponents();
        setTitle("Acta: " + reunion.motivo());
        jLabel2.setText(" " + reunion.comite() + " ");
        jLabel8.setText(" " + reunion.fecha() + "  " + reunion.horaInicio() + " ");
        jLabel8.setToolTipText(reunion.motivo());
        lstLista.setModel(convocados);
        lstLista.setCellRenderer(new CheckListRenderer());
        Ui.run(this, () -> {
            ctx.miembros().findByComite(reunion.comiteId()).forEach(m -> convocados.addElement(new CheckListItem<>(m)));
            List<Tema> temas = ctx.temas().findByReunion(reunion.id());
            Ui.fill(cbxTemas, temas);
            Ui.fill(cbxTipoConclusion, ctx.catalogos().tiposResolucion());
            if (temas.isEmpty()) {
                Ui.warn(this, "La reunión no tiene temas en su orden del día.\n"
                        + "Agréguelos en Mantenimiento › Reuniones antes de registrar intervenciones o resoluciones.");
            }
        });
        Ui.fill(cbxPresentes, Collections.emptyList());
        // Permite marcar asistentes también con la barra espaciadora
        lstLista.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
                    alternar(lstLista.getSelectedIndex());
                }
            }
        });
    }

    private void alternar(int indice) {
        if (indice < 0) {
            return;
        }
        convocados.get(indice).toggle();
        lstLista.repaint(lstLista.getCellBounds(indice, indice));
        sincronizarPresentes();
    }

    /** El combo de presentes refleja exactamente los miembros marcados (antes se duplicaban). */
    private void sincronizarPresentes() {
        Opcion anterior = Ui.selected(cbxPresentes);
        Ui.fill(cbxPresentes, presentes());
        if (anterior != null) {
            Ui.selectById(cbxPresentes, anterior.id());
        }
    }

    private List<Opcion> presentes() {
        return Collections.list(convocados.elements()).stream()
                .filter(CheckListItem::isSelected)
                .map(CheckListItem::value)
                .toList();
    }

    private void actualizarEstado() {
        setTitle("Acta: " + reunion.motivo() + "   [" + intervencionesRegistradas + " intervenciones, "
                + resolucionesRegistradas + " resoluciones]");
    }

    private void formInternalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
        lstLista.requestFocusInWindow();
    }

    private void lstListaMouseClicked(java.awt.event.MouseEvent evt) {
        alternar(lstLista.locationToIndex(evt.getPoint()));
    }

    private void btnAgregarIntervencionActionPerformed(java.awt.event.ActionEvent evt) {
        Opcion miembro = Ui.selected(cbxPresentes);
        Tema tema = (Tema) cbxTemas.getSelectedItem();
        String texto = txtIntervencion.getText().trim();
        if (miembro == null || tema == null || texto.isEmpty()) {
            Ui.warn(this, "Marque al miembro como presente, elija el tema y escriba la intervención.");
            return;
        }
        if (Ui.run(this, () -> ctx.actas().agregarIntervencion(reunion.id(), miembro.id(), tema.id(), texto))) {
            intervencionesRegistradas++;
            txtIntervencion.setText("");
            actualizarEstado();
        }
    }

    private void btnAgregarConclusionActionPerformed(java.awt.event.ActionEvent evt) {
        Opcion tipo = Ui.selected(cbxTipoConclusion);
        Tema tema = (Tema) cbxTemas.getSelectedItem();
        String texto = txtConclusion.getText().trim();
        if (tipo == null || tema == null || texto.isEmpty()) {
            Ui.warn(this, "Elija el tema y el tipo, y escriba la resolución.");
            return;
        }
        if (Ui.run(this, () -> ctx.actas().agregarResolucion(reunion.id(), tipo.id(), tema.id(), texto))) {
            resolucionesRegistradas++;
            txtConclusion.setText("");
            actualizarEstado();
        }
    }

    private void btnCerrarActionPerformed(java.awt.event.ActionEvent evt) {
        List<Opcion> presentes = presentes();
        if (presentes.isEmpty()) {
            Ui.warn(this, "Marque al menos un miembro presente antes de cerrar la reunión.");
            return;
        }
        if (!Ui.confirm(this, "Se registrará la asistencia de " + presentes.size() + " de " + convocados.size()
                + " convocados y la reunión quedará cerrada (ya no podrá modificarse).\n¿Continuar?")) {
            return;
        }
        if (Ui.run(this, () -> ctx.reuniones().cerrar(reunion.id(), presentes.stream().map(Opcion::id).toList()))) {
            Ui.info(this, "Reunión cerrada. El acta está disponible en Reuniones › Cerradas.");
            dispose();
        }
    }

    /** Construye el layout (código heredado del diseñador de NetBeans, sin lógica). */
        private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lstLista = new javax.swing.JList<>();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        txtIntervencion = new javax.swing.JTextArea();
        cbxPresentes = new javax.swing.JComboBox<>();
        cbxTemas = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        btnAgregarConclusion = new javax.swing.JButton();
        btnAgregarIntervencion = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtConclusion = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cbxTipoConclusion = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        btnCerrar = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();

        setClosable(true);
        setIconifiable(true);
        setTitle("Reunión");
        addInternalFrameListener(new javax.swing.event.InternalFrameListener() {
            public void internalFrameActivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameClosing(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameIconified(javax.swing.event.InternalFrameEvent evt) {
            }
            public void internalFrameOpened(javax.swing.event.InternalFrameEvent evt) {
                formInternalFrameOpened(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Lista", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        lstLista.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lstListaMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(lstLista);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 381, Short.MAX_VALUE)
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Intervenciones", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        txtIntervencion.setColumns(20);
        txtIntervencion.setRows(5);
        jScrollPane2.setViewportView(txtIntervencion);

        jLabel3.setText("Temas");

        btnAgregarConclusion.setText("Agregar resolución");
        btnAgregarConclusion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarConclusionActionPerformed(evt);
            }
        });

        btnAgregarIntervencion.setText("Agregar intervención");
        btnAgregarIntervencion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarIntervencionActionPerformed(evt);
            }
        });

        txtConclusion.setColumns(20);
        txtConclusion.setRows(5);
        jScrollPane3.setViewportView(txtConclusion);

        jLabel4.setText("Intervención");

        jLabel5.setText("Resoluciones");

        jLabel6.setText("Presentes");

        jLabel9.setText("Tipo de resolución");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(19, 19, 19)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel4)
                                    .addComponent(jLabel3)
                                    .addComponent(cbxTemas, javax.swing.GroupLayout.PREFERRED_SIZE, 445, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGap(36, 36, 36)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel6)
                                    .addComponent(cbxPresentes, javax.swing.GroupLayout.PREFERRED_SIZE, 282, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 763, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 763, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel9)
                            .addComponent(btnAgregarIntervencion)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cbxTipoConclusion, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(btnAgregarConclusion, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbxTemas, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbxPresentes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 167, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5))
                    .addComponent(btnAgregarIntervencion))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 124, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbxTipoConclusion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAgregarConclusion)))
                .addGap(22, 22, 22))
        );

        btnCerrar.setText("Cerrar reunión y registrar asistencia");
        btnCerrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCerrarActionPerformed(evt);
            }
        });

        jLabel1.setText("Comité");

        jLabel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel7.setText("Reunión");

        jLabel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel7))
                        .addGap(27, 27, 27)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(59, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(1102, Short.MAX_VALUE)
                .addComponent(btnCerrar)
                .addGap(82, 82, 82))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(24, 24, 24)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel7)
                            .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, 496, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addComponent(btnCerrar)
                .addGap(239, 239, 239))
        );

        pack();
    }

    // Componentes Swing (layout heredado del diseñador de NetBeans)
    private javax.swing.JButton btnAgregarConclusion;
    private javax.swing.JButton btnAgregarIntervencion;
    private javax.swing.JButton btnCerrar;
    private javax.swing.JComboBox<Opcion> cbxPresentes;
    private javax.swing.JComboBox<Tema> cbxTemas;
    private javax.swing.JComboBox<Opcion> cbxTipoConclusion;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JList<CheckListItem<Opcion>> lstLista;
    private javax.swing.JTextArea txtConclusion;
    private javax.swing.JTextArea txtIntervencion;
}
