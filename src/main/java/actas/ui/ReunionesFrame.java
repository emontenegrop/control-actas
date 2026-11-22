package actas.ui;

import static actas.ui.components.ListTableModel.col;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import javax.swing.ListSelectionModel;

import actas.AppContext;
import actas.model.Opcion;
import actas.model.Reunion;
import actas.model.Tema;
import actas.service.ReunionService;
import actas.service.Validaciones;
import actas.ui.components.ListTableModel;

/** Programación de reuniones y de su orden del día (temas). */
public class ReunionesFrame extends javax.swing.JInternalFrame {

    private final AppContext ctx;
    private final ListTableModel<Reunion> modeloReuniones = new ListTableModel<>(
            col("Código", Reunion::id),
            col("Fecha", Reunion::fecha),
            col("Inicio", Reunion::horaInicio),
            col("Fin", Reunion::horaFin),
            col("Comité", Reunion::comite),
            col("Tipo", Reunion::tipoReunion),
            col("Motivo", Reunion::motivo),
            col("Estado", Reunion::estado));
    private final ListTableModel<Tema> modeloTemas = new ListTableModel<>(
            col("N.º", Tema::numero),
            col("Tema", Tema::tema));
    private Reunion seleccionada;
    private Tema temaSeleccionado;

    public ReunionesFrame(AppContext ctx) {
        this.ctx = ctx;
        initComponents();
        tblReuniones.setModel(modeloReuniones);
        tblTemas.setModel(modeloTemas);
        tblTemas.getColumnModel().getColumn(0).setMaxWidth(50);
        tblReuniones.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblTemas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblReuniones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                modeloReuniones.rowAt(tblReuniones.getSelectedRow()).ifPresent(this::mostrar);
            }
        });
        tblTemas.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                modeloTemas.rowAt(tblTemas.getSelectedRow()).ifPresent(this::mostrarTema);
            }
        });
        btnBuscar.addActionListener(evt -> buscar());
        txtBuscar.addActionListener(evt -> buscar());
        Ui.run(this, () -> {
            Ui.fill(cbxTipo, ctx.catalogos().tiposReunion());
            Ui.fill(cbxComites, ctx.catalogos().comites());
            modeloReuniones.setRows(ctx.reuniones().findAll());
        });
        limpiar();
    }

    private void mostrar(Reunion r) {
        seleccionada = r;
        txtMotivo.setText(r.motivo());
        txtFecha.setText(r.fecha().format(Validaciones.FECHA));
        txtHIni.setText(r.horaInicio().format(Validaciones.HORA));
        txtHFin.setText(r.horaFin() == null ? "" : r.horaFin().format(Validaciones.HORA));
        txtLugar.setText(r.lugar() == null ? "" : r.lugar());
        Ui.selectById(cbxTipo, r.tipoReunionId());
        Ui.selectById(cbxComites, r.comiteId());
        boolean editable = !r.cerrada();
        btnActualizar.setEnabled(editable);
        btnBorrar.setEnabled(true);
        btnInsertar.setEnabled(false);
        btnInsertarTema.setEnabled(editable);
        cargarTemas();
    }

    private void cargarTemas() {
        temaSeleccionado = null;
        txtTema.setText("");
        btnActTemas.setEnabled(false);
        Ui.run(this, () -> modeloTemas.setRows(
                seleccionada == null ? List.of() : ctx.temas().findByReunion(seleccionada.id())));
    }

    private void mostrarTema(Tema t) {
        temaSeleccionado = t;
        txtTema.setText(t.tema());
        btnActTemas.setEnabled(seleccionada != null && !seleccionada.cerrada());
    }

    private void limpiar() {
        seleccionada = null;
        tblReuniones.clearSelection();
        txtMotivo.setText("");
        txtFecha.setText(LocalDate.now().format(Validaciones.FECHA));
        txtHIni.setText("");
        txtHFin.setText("");
        txtLugar.setText("");
        btnActualizar.setEnabled(false);
        btnBorrar.setEnabled(false);
        btnInsertar.setEnabled(true);
        btnInsertarTema.setEnabled(false);
        modeloTemas.setRows(List.of());
        txtTema.setText("");
        btnActTemas.setEnabled(false);
    }

    private ReunionService.Formulario formulario(Integer id) {
        return new ReunionService.Formulario(id, txtMotivo.getText(), txtFecha.getText(), txtHIni.getText(),
                txtHFin.getText(), txtLugar.getText(), Ui.selected(cbxTipo), Ui.selected(cbxComites));
    }

    private void guardar(Integer id) {
        Ui.run(this, () -> {
            Reunion r = ctx.reunionService().guardar(formulario(id), ctx.usuarioActualId());
            modeloReuniones.setRows(ctx.reuniones().findAll());
            seleccionar(r.id());
        });
    }

    private void seleccionar(int id) {
        for (int i = 0; i < modeloReuniones.getRowCount(); i++) {
            if (modeloReuniones.getRow(i).id() == id) {
                tblReuniones.setRowSelectionInterval(i, i);
                return;
            }
        }
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        Ui.run(this, () -> {
            LocalDate fecha = Validaciones.parseFecha(texto);
            List<Reunion> resultado = texto.isEmpty() ? ctx.reuniones().findAll()
                    : fecha != null ? ctx.reuniones().findAll().stream().filter(r -> r.fecha().equals(fecha)).toList()
                    : ctx.reuniones().buscarPorMotivo(texto);
            modeloReuniones.setRows(resultado);
            limpiar();
            if (resultado.isEmpty()) {
                Ui.info(this, "No se encontraron reuniones.");
            }
        });
    }

    private void mover(int delta) {
        int total = modeloReuniones.getRowCount();
        if (total > 0) {
            int i = Math.clamp(tblReuniones.getSelectedRow() + delta, 0, total - 1);
            tblReuniones.setRowSelectionInterval(i, i);
        }
    }

    private void tblReunionesMouseClicked(java.awt.event.MouseEvent evt) {
        // gestionado por el ListSelectionListener
    }

    private void tblTemasMouseClicked(java.awt.event.MouseEvent evt) {
        // gestionado por el ListSelectionListener
    }

    private void btnNuevoActionPerformed(java.awt.event.ActionEvent evt) {
        limpiar();
    }

    private void btnInsertarActionPerformed(java.awt.event.ActionEvent evt) {
        guardar(null);
    }

    private void btnActualizarActionPerformed(java.awt.event.ActionEvent evt) {
        if (seleccionada != null) {
            guardar(seleccionada.id());
        }
    }

    private void btnBorrarActionPerformed(java.awt.event.ActionEvent evt) {
        if (seleccionada == null) {
            return;
        }
        String aviso = seleccionada.cerrada()
                ? "La reunión está CERRADA: se eliminará también su acta (asistencias, intervenciones y resoluciones).\n¿Continuar?"
                : "¿Eliminar la reunión \"" + seleccionada.motivo() + "\" y sus temas?";
        if (Ui.confirm(this, aviso)) {
            Ui.run(this, () -> {
                ctx.reuniones().delete(seleccionada.id());
                modeloReuniones.setRows(ctx.reuniones().findAll());
                limpiar();
            });
        }
    }

    private void btnAtrasActionPerformed(java.awt.event.ActionEvent evt) {
        mover(-1);
    }

    private void btnAvanceActionPerformed(java.awt.event.ActionEvent evt) {
        mover(1);
    }

    private void btnNuevoTemaActionPerformed(java.awt.event.ActionEvent evt) {
        temaSeleccionado = null;
        tblTemas.clearSelection();
        txtTema.setText("");
        btnActTemas.setEnabled(false);
        txtTema.requestFocusInWindow();
    }

    private Optional<String> textoTema() {
        String texto = txtTema.getText().trim();
        if (texto.isEmpty() || texto.length() > 200) {
            Ui.warn(this, "El tema es obligatorio y admite como máximo 200 caracteres.");
            return Optional.empty();
        }
        return Optional.of(texto);
    }

    private void btnInsertarTemaActionPerformed(java.awt.event.ActionEvent evt) {
        if (seleccionada == null) {
            Ui.warn(this, "Seleccione primero una reunión.");
            return;
        }
        textoTema().ifPresent(texto -> Ui.run(this, () -> {
            ctx.temas().agregar(seleccionada.id(), texto);
            cargarTemas();
        }));
    }

    private void btnActTemasActionPerformed(java.awt.event.ActionEvent evt) {
        if (temaSeleccionado == null) {
            return;
        }
        textoTema().ifPresent(texto -> Ui.run(this, () -> {
            ctx.temas().actualizar(temaSeleccionado.id(), texto);
            cargarTemas();
        }));
    }

    /** Construye el layout (código heredado del diseñador de NetBeans, sin lógica). */
        private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblReuniones = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        txtFecha = new javax.swing.JTextField();
        txtHFin = new javax.swing.JTextField();
        txtHIni = new javax.swing.JTextField();
        txtLugar = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        cbxTipo = new javax.swing.JComboBox<>();
        btnNuevo = new javax.swing.JButton();
        btnInsertar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnBorrar = new javax.swing.JButton();
        btnAtras = new javax.swing.JButton();
        btnAvance = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        txtMotivo = new javax.swing.JTextArea();
        cbxComites = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        tblTemas = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        btnNuevoTema = new javax.swing.JButton();
        btnInsertarTema = new javax.swing.JButton();
        btnActTemas = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        txtTema = new javax.swing.JTextArea();
        jPanel3 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setTitle("Programar reuniones");

        tblReuniones.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblReuniones.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblReunionesMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tblReuniones);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mantenimiento", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        jLabel1.setText("Fecha (aaaa-mm-dd)");

        jLabel2.setText("Motivo");

        jLabel3.setText("Hora inicio (HH:mm)");

        jLabel4.setText("Hora fin (HH:mm)");

        jLabel5.setText("Lugar");

        jLabel6.setText("Tipo");

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

        txtMotivo.setColumns(20);
        txtMotivo.setRows(5);
        jScrollPane3.setViewportView(txtMotivo);

        jLabel9.setText("Comité");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(30, 30, 30)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel5)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3)
                    .addComponent(jLabel6)
                    .addComponent(jLabel2)
                    .addComponent(jLabel9))
                .addGap(21, 21, 21)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(btnNuevo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnInsertar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnActualizar)
                        .addGap(10, 10, 10)
                        .addComponent(btnBorrar))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(3, 3, 3)
                        .addComponent(btnAtras)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAvance))
                    .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(cbxComites, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(txtHIni, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtFecha, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(txtLugar, javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(cbxTipo, javax.swing.GroupLayout.Alignment.LEADING, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtHFin, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 263, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 323, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel1)
                            .addComponent(txtFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(txtHIni, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel4)
                            .addComponent(txtHFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtLugar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(cbxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(cbxComites, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel9))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAtras)
                            .addComponent(btnAvance))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnInsertar)
                            .addComponent(btnBorrar)
                            .addComponent(btnActualizar)
                            .addComponent(btnNuevo)))
                    .addComponent(jLabel2))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Temas", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        tblTemas.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tblTemasMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tblTemas);

        jLabel8.setText("Tema");

        btnNuevoTema.setText("Nuevo");
        btnNuevoTema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNuevoTemaActionPerformed(evt);
            }
        });

        btnInsertarTema.setText("Insertar");
        btnInsertarTema.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInsertarTemaActionPerformed(evt);
            }
        });

        btnActTemas.setText("Actualizar");
        btnActTemas.setEnabled(false);
        btnActTemas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnActTemasActionPerformed(evt);
            }
        });

        txtTema.setColumns(20);
        txtTema.setRows(5);
        jScrollPane4.setViewportView(txtTema);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 413, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel8)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(btnNuevoTema)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnInsertarTema)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnActTemas))
                            .addComponent(jScrollPane4, javax.swing.GroupLayout.DEFAULT_SIZE, 361, Short.MAX_VALUE))))
                .addGap(18, 18, 18))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 144, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnNuevoTema)
                            .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(btnInsertarTema)
                                .addComponent(btnActTemas))))
                    .addComponent(jLabel8))
                .addContainerGap(30, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Búsqueda", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        jLabel7.setText("Motivo o fecha");

        btnBuscar.setText("Buscar");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBuscar)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(47, 47, 47)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(btnBuscar)
                .addContainerGap(60, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 1174, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 1186, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }

    // Componentes Swing (layout heredado del diseñador de NetBeans)
    private javax.swing.JButton btnActTemas;
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnAtras;
    private javax.swing.JButton btnAvance;
    private javax.swing.JButton btnBorrar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JButton btnInsertar;
    private javax.swing.JButton btnInsertarTema;
    private javax.swing.JComboBox<Opcion> cbxComites;
    private javax.swing.JComboBox<Opcion> cbxTipo;
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
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JTable tblReuniones;
    private javax.swing.JTable tblTemas;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtFecha;
    private javax.swing.JTextField txtHFin;
    private javax.swing.JTextField txtHIni;
    private javax.swing.JTextField txtLugar;
    private javax.swing.JTextArea txtMotivo;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnNuevoTema;
    private javax.swing.JTextArea txtTema;
}
