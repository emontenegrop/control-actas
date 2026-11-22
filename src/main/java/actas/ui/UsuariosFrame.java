package actas.ui;

import static actas.ui.components.ListTableModel.col;

import java.util.Arrays;
import java.util.List;
import javax.swing.ListSelectionModel;

import actas.AppContext;
import actas.model.Opcion;
import actas.model.Usuario;
import actas.service.Validaciones;
import actas.ui.components.ListTableModel;

/**
 * Administración de usuarios. En el original este formulario existía pero ningún botón tenía
 * funcionalidad. Las claves se guardan con hash PBKDF2.
 */
public class UsuariosFrame extends javax.swing.JInternalFrame {

    private final AppContext ctx;
    private final ListTableModel<Usuario> modelo = new ListTableModel<>(
            col("Código", Usuario::id),
            col("Usuario", Usuario::usuario),
            col("Cédula", Usuario::cedula),
            col("Tipo", Usuario::tipoUsuario));
    private Usuario seleccionado;

    public UsuariosFrame(AppContext ctx) {
        this.ctx = ctx;
        initComponents();
        tblUsuarios.setModel(modelo);
        tblUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tblUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                modelo.rowAt(tblUsuarios.getSelectedRow()).ifPresent(this::mostrar);
            }
        });
        txtClave.setToolTipText("Al actualizar, deje la clave vacía para conservar la actual");
        rdbNombre.setSelected(true);
        btnNuevo.addActionListener(evt -> limpiar());
        btnInsertar.addActionListener(evt -> guardar(true));
        btnActualizar.addActionListener(evt -> guardar(false));
        btnBorrar.addActionListener(evt -> borrar());
        btnBuscar.addActionListener(evt -> buscar());
        txtBuscar.addActionListener(evt -> buscar());
        Ui.run(this, () -> {
            Ui.fill(cbxTipo, ctx.catalogos().tiposUsuario());
            modelo.setRows(ctx.usuarios().findAll());
        });
        limpiar();
    }

    private void mostrar(Usuario u) {
        seleccionado = u;
        txtUsuario.setText(u.usuario());
        txtCedula.setText(u.cedula() == null ? "" : u.cedula());
        txtClave.setText("");
        Ui.selectById(cbxTipo, u.tipoUsuarioId());
        btnActualizar.setEnabled(true);
        btnBorrar.setEnabled(!u.id().equals(ctx.usuarioActualId()));
        btnInsertar.setEnabled(false);
    }

    private void limpiar() {
        seleccionado = null;
        tblUsuarios.clearSelection();
        txtUsuario.setText("");
        txtCedula.setText("");
        txtClave.setText("");
        if (cbxTipo.getItemCount() > 0) {
            cbxTipo.setSelectedIndex(cbxTipo.getItemCount() - 1);
        }
        btnActualizar.setEnabled(false);
        btnBorrar.setEnabled(false);
        btnInsertar.setEnabled(true);
    }

    private void guardar(boolean nuevo) {
        String usuario = txtUsuario.getText().trim();
        String cedula = txtCedula.getText().trim();
        Opcion tipo = Ui.selected(cbxTipo);
        char[] clave = txtClave.getPassword();
        try {
            Ui.run(this, () -> {
                new Validaciones.Errores()
                        .requerido(usuario, "El usuario")
                        .si(!usuario.matches("[A-Za-z0-9._-]{3,75}"),
                                "El usuario debe tener 3 a 75 caracteres (letras, números, . _ -).")
                        .cedulaOpcional(cedula)
                        .si(tipo == null, "Seleccione el tipo de usuario.")
                        .si(nuevo && clave.length < 8, "La clave debe tener al menos 8 caracteres.")
                        .si(!nuevo && clave.length > 0 && clave.length < 8, "La nueva clave debe tener al menos 8 caracteres.")
                        .lanzarSiHay();
                String ced = Validaciones.nullSiVacio(cedula);
                if (nuevo) {
                    ctx.usuarios().insert(usuario, ced, tipo.id(), clave);
                } else {
                    ctx.usuarios().update(seleccionado.id(), usuario, ced, tipo.id(), clave);
                }
                modelo.setRows(ctx.usuarios().findAll());
                limpiar();
                Ui.info(this, nuevo ? "Usuario creado." : "Usuario actualizado.");
            });
        } finally {
            Arrays.fill(clave, '\0');
        }
    }

    private void borrar() {
        if (seleccionado == null || !Ui.confirm(this, "¿Eliminar el usuario " + seleccionado.usuario() + "?")) {
            return;
        }
        Ui.run(this, () -> {
            ctx.usuarios().delete(seleccionado.id());
            modelo.setRows(ctx.usuarios().findAll());
            limpiar();
        });
    }

    private void buscar() {
        String texto = txtBuscar.getText().trim();
        Ui.run(this, () -> {
            List<Usuario> r = texto.isEmpty() ? ctx.usuarios().findAll()
                    : ctx.usuarios().buscar(texto, rdbCedula.isSelected());
            modelo.setRows(r);
            limpiar();
        });
    }

    /** Construye el layout (código heredado del diseñador de NetBeans, sin lógica). */
        private void initComponents() {

        btngBusqueda = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblUsuarios = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        btnNuevo = new javax.swing.JButton();
        txtUsuario = new javax.swing.JTextField();
        txtCedula = new javax.swing.JTextField();
        cbxTipo = new javax.swing.JComboBox<>();
        btnInsertar = new javax.swing.JButton();
        btnActualizar = new javax.swing.JButton();
        btnBorrar = new javax.swing.JButton();
        txtClave = new javax.swing.JPasswordField();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        rdbNombre = new javax.swing.JRadioButton();
        rdbCedula = new javax.swing.JRadioButton();
        txtBuscar = new javax.swing.JTextField();
        btnBuscar = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setTitle("Usuarios");

        jScrollPane1.setViewportView(tblUsuarios);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mantenimiento", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        jLabel1.setText("Usuario");

        jLabel2.setText("Cédula");

        jLabel3.setText("Tipo");

        btnNuevo.setText("Nuevo");

        btnInsertar.setText("Insertar");

        btnActualizar.setText("Actualizar");

        btnBorrar.setText("Borrar");

        jLabel4.setText("Clave");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(36, 36, 36)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(jLabel4)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 37, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(btnNuevo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnInsertar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnActualizar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnBorrar))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(txtUsuario)
                        .addComponent(txtCedula, javax.swing.GroupLayout.DEFAULT_SIZE, 340, Short.MAX_VALUE)
                        .addComponent(txtClave, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 171, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cbxTipo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 176, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(10, 10, 10))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtCedula, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtClave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cbxTipo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnInsertar)
                    .addComponent(btnActualizar)
                    .addComponent(btnNuevo)
                    .addComponent(btnBorrar))
                .addContainerGap(31, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Búsqueda", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null));

        rdbNombre.setText("Por usuario");

        rdbCedula.setText("Por cédula");

        btnBuscar.setText("Buscar");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(57, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnBuscar)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 139, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(52, 52, 52)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(rdbCedula)
                    .addComponent(rdbNombre))
                .addContainerGap(55, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(rdbNombre)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rdbCedula)
                .addGap(18, 18, 18)
                .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnBuscar)
                .addContainerGap(34, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 720, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    // Componentes Swing (layout heredado del diseñador de NetBeans)
    private javax.swing.ButtonGroup btngBusqueda;
    private javax.swing.JButton btnNuevo;
    private javax.swing.JButton btnInsertar;
    private javax.swing.JButton btnActualizar;
    private javax.swing.JButton btnBorrar;
    private javax.swing.JButton btnBuscar;
    private javax.swing.JComboBox<Opcion> cbxTipo;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPasswordField txtClave;
    private javax.swing.JRadioButton rdbNombre;
    private javax.swing.JRadioButton rdbCedula;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable tblUsuarios;
    private javax.swing.JTextField txtUsuario;
    private javax.swing.JTextField txtCedula;
    private javax.swing.JTextField txtBuscar;
}
