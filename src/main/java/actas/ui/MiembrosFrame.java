package actas.ui;

import static actas.ui.components.ListTableModel.col;

import java.util.List;
import javax.swing.ListSelectionModel;

import actas.AppContext;
import actas.model.Miembro;
import actas.model.Opcion;
import actas.ui.components.ListTableModel;

/** Mantenimiento de miembros de los comités. */
public class MiembrosFrame extends javax.swing.JInternalFrame {

    private final AppContext ctx;
    private final ListTableModel<Miembro> modelo = new ListTableModel<>(
            col("Código", Miembro::id),
            col("Nombre", Miembro::nombre),
            col("Cédula", Miembro::cedula),
            col("Teléfono", Miembro::telefono),
            col("Celular", Miembro::celular),
            col("Correo", Miembro::correo),
            col("Cargo", Miembro::cargo),
            col("Comité", Miembro::comite));
    /** Miembro cargado en el formulario; {@code null} cuando se está creando uno nuevo. */
    private Miembro seleccionado;

    public MiembrosFrame(AppContext ctx) {
        this.ctx = ctx;
        initComponents();
        tblMantenimientoMiembros.setModel(modelo);
        tblMantenimientoMiembros.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblMantenimientoMiembros.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                modelo.rowAt(tblMantenimientoMiembros.getSelectedRow()).ifPresent(this::mostrar);
            }
        });
        rdbNombre.setSelected(true);
        txtBuscar.addActionListener(evt -> btnBuscarActionPerformed(evt));
        Ui.run(this, () -> {
            Ui.fill(cbxCargo, ctx.catalogos().cargos());
            Ui.fill(cbxComite, ctx.catalogos().comites());
            recargar(ctx.miembros().findAll());
        });
        limpiar();
    }

    private void recargar(List<Miembro> miembros) {
        modelo.setRows(miembros);
    }

    private void mostrar(Miembro m) {
        seleccionado = m;
        txtNombre.setText(m.nombre());
        txtCedula.setText(nullToEmpty(m.cedula()));
        txtTelefono.setText(nullToEmpty(m.telefono()));
        txtCelular.setText(nullToEmpty(m.celular()));
        txtCorreo.setText(nullToEmpty(m.correo()));
        Ui.selectById(cbxCargo, m.cargoId());
        Ui.selectById(cbxComite, m.comiteId());
        btnActualizar.setEnabled(true);
        btnBorrar.setEnabled(true);
        btnInsertar.setEnabled(false);
    }

    private void limpiar() {
        seleccionado = null;
        tblMantenimientoMiembros.clearSelection();
        for (var campo : List.of(txtNombre, txtCedula, txtTelefono, txtCelular, txtCorreo)) {
            campo.setText("");
        }
        if (cbxCargo.getItemCount() > 0) {
            cbxCargo.setSelectedIndex(0);
        }
        if (cbxComite.getItemCount() > 0) {
            cbxComite.setSelectedIndex(0);
        }
        btnActualizar.setEnabled(false);
        btnBorrar.setEnabled(false);
        btnInsertar.setEnabled(true);
        txtNombre.requestFocusInWindow();
    }

    private Miembro leerFormulario(Integer id) {
        Opcion cargo = Ui.selected(cbxCargo);
        Opcion comite = Ui.selected(cbxComite);
        return new Miembro(id, txtNombre.getText(), txtCedula.getText(), txtTelefono.getText(),
                txtCelular.getText(), txtCorreo.getText(), cargo == null ? 0 : cargo.id(), null,
                comite == null ? null : comite.id(), null);
    }

    private void guardar(Integer id) {
        Ui.run(this, () -> {
            Miembro guardado = ctx.miembroService().guardar(leerFormulario(id));
            recargar(ctx.miembros().findAll());
            seleccionar(guardado.id());
        });
    }

    private void seleccionar(int id) {
        for (int i = 0; i < modelo.getRowCount(); i++) {
            if (modelo.getRow(i).id() == id) {
                tblMantenimientoMiembros.setRowSelectionInterval(i, i);
                tblMantenimientoMiembros.scrollRectToVisible(tblMantenimientoMiembros.getCellRect(i, 0, true));
                return;
            }
        }
    }

    private void mover(int delta) {
        int total = modelo.getRowCount();
        if (total == 0) {
            return;
        }
        int actual = tblMantenimientoMiembros.getSelectedRow();
        int siguiente = Math.clamp(actual + delta, 0, total - 1);
        tblMantenimientoMiembros.setRowSelectionInterval(siguiente, siguiente);
        tblMantenimientoMiembros.scrollRectToVisible(tblMantenimientoMiembros.getCellRect(siguiente, 0, true));
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private void tblMantenimientoMiembrosMouseClicked(java.awt.event.MouseEvent evt) {
        // la selección se gestiona con el ListSelectionListener (también funciona con teclado)
    }

    private void btnAtrasActionPerformed(java.awt.event.ActionEvent evt) {
        mover(-1);
    }

    private void btnAvanceActionPerformed(java.awt.event.ActionEvent evt) {
        mover(1);
    }

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {
        limpiar();
    }

    private void btnInsertarActionPerformed(java.awt.event.ActionEvent evt) {
        guardar(null);
    }

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {
        if (seleccionado != null) {
            guardar(seleccionado.id());
        }
    }

    private void btnBorrarActionPerformed(java.awt.event.ActionEvent evt) {
        if (seleccionado == null || !Ui.confirm(this, "¿Eliminar a " + seleccionado.nombre() + "?")) {
            return;
        }
        Ui.run(this, () -> {
            ctx.miembros().delete(seleccionado.id());
            recargar(ctx.miembros().findAll());
            limpiar();
        });
    }

    private void btnBuscarActionPerformed(java.awt.event.ActionEvent evt) {
        String texto = txtBuscar.getText().trim();
        Ui.run(this, () -> {
            if (texto.isEmpty()) {
                recargar(ctx.miembros().findAll());
            } else if (rdbCodigo.isSelected()) {
                if (!texto.matches("\\d{1,9}")) {
                    Ui.warn(this, "El código debe ser numérico.");
                    return;
                }
                recargar(ctx.miembros().findById(Integer.parseInt(texto)).map(List::of).orElse(List.of()));
            } else {
                recargar(ctx.miembros().buscarPorNombre(texto));
            }
            if (modelo.getRowCount() > 0) {
                tblMantenimientoMiembros.setRowSelectionInterval(0, 0);
            } else {
                Ui.info(this, "No se encontraron miembros.");
            }
        });
    }

    /** Construye el layout (código heredado del diseñador de NetBeans, sin lógica). */
        private void initComponents() {

        btngBusquedaMiembros = new javax.swing.ButtonGroup();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jpnMantenimiento = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblMantenimientoMiembros = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        txtNombre = new javax.swing.JTextField();
        txtCorreo = new javax.swing.JTextField();
        txtCelular = new javax.swing.JTextField();
        txtTelefono = new javax.swing.JTextField();
        txtCedula = new javax.swing.JTextField();
        btnAtras = new javax.swing.JButton();
        btnAvance = new javax.swing.JButton();
        btnNuevo = new javax.swing.JButton();
        btnInsertar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnBorrar = new javax.swing.JButton();
        cbxComite = new javax.swing.JComboBox<>();
        cbxCargo = new javax.swing.JComboBox<>();
        jPanel2 = new javax.swing.JPanel();
        rdbCodigo = new javax.swing.JRadioButton();
        rdbNombre = new javax.swing.JRadioButton();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setTitle("Miembros");

        tblMantenimientoMiembros.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblMantenimientoMiembros.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblMantenimientoMiembrosMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblMantenimientoMiembros);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mantenimiento", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        jLabel1.setText("Nombre");

        jLabel5.setText("Correo");

        jLabel4.setText("Celular");

        jLabel3.setText("Teléfono");

        jLabel2.setText("Cédula");

        jLabel7.setText("Cargo");

        jLabel6.setText("Comité");

        btnAtras.setText("<<");
        btnAtras.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAtrasActionPerformed(evt);
            }
        });

        btnAvance.setText(">>");
        btnAvance.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAvanceActionPerformed(evt);
            }
        });

        btnNuevo.setText("Nuevo");
        btnNuevo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoActionPerformed(evt);
            }
        });

        btnInsertar.setText("Insertar");
        btnInsertar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertarActionPerformed(evt);
            }
        });

        btnActualizar.setText("Actualizar");
        btnActualizar.setEnabled(false);
        btnActualizar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActualizarActionPerformed(evt);
            }
        });

        btnBorrar.setText("Borrar");
        btnBorrar.setEnabled(false);
        btnBorrar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBorrarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel7)
                    .addComponent(jLabel6))
                .addGap(34, 34, 34)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                .addComponent(btnAtras)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnAvance))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(btnNuevo)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnInsertar)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnActualizar)
                        .addGap(10, 10, 10)
                        .addComponent(btnBorrar))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(cbxComite, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(cbxCargo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 221, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtNombre, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtCorreo, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtCelular, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtTelefono, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtCedula, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 309, Short.MAX_VALUE)))
                .addContainerGap(18, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(txtNombre, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(9, 9, 9)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel2)
                    .addComponent(txtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel3)
                    .addComponent(txtTelefono, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel4)
                    .addComponent(txtCelular, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel5)
                    .addComponent(txtCorreo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(cbxCargo, javax.swing.GroupLayout.PREFERRED_SIZE, 20, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(cbxComite, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAtras)
                    .addComponent(btnAvance))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnInsertar)
                    .addComponent(btnBorrar)
                    .addComponent(btnActualizar)
                    .addComponent(btnNuevo))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Búsqueda", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        buttonGroup1.add(rdbCodigo);
        rdbCodigo.setText("Por código");

        buttonGroup1.add(rdbNombre);
        rdbNombre.setText("Por nombre");

        btnBuscar.setText("Buscar");
        btnBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBuscarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(61, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBuscar)
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                            .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 229, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(rdbNombre)
                                .addComponent(rdbCodigo))
                            .addGap(140, 140, 140))))
                .addGap(10, 10, 10))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(28, 28, 28)
                .addComponent(rdbCodigo)
                .addGap(5, 5, 5)
                .addComponent(rdbNombre)
                .addGap(30, 30, 30)
                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBuscar)
                .addContainerGap(102, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jpnMantenimientoLayout = new javax.swing.GroupLayout(jpnMantenimiento);
        jpnMantenimiento.setLayout(jpnMantenimientoLayout);
        jpnMantenimientoLayout.setHorizontalGroup(
            jpnMantenimientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpnMantenimientoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jpnMantenimientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jpnMantenimientoLayout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 811, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(19, Short.MAX_VALUE))
        );
        jpnMantenimientoLayout.setVerticalGroup(
            jpnMantenimientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jpnMantenimientoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jpnMantenimientoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Mantenimiento", jpnMantenimiento);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 848, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 579, Short.MAX_VALUE)
        );

        pack();
    }

    // Componentes Swing (layout heredado del diseñador de NetBeans)
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAtras;
    private javax.swing.JButton btnAvance;
    private javax.swing.JButton btnBorrar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnInsertar;
    private javax.swing.ButtonGroup btngBusquedaMiembros;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<Opcion> cbxCargo;
    private javax.swing.JComboBox<Opcion> cbxComite;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JRadioButton rdbNombre;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel jpnMantenimiento;
    private javax.swing.JRadioButton rdbCodigo;
    private javax.swing.JTable tblMantenimientoMiembros;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCedula;
    private javax.swing.JTextField txtCelular;
    private javax.swing.JTextField txtCorreo;
    private javax.swing.JTextField txtNombre;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JTextField txtTelefono;
}
