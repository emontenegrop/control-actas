package actas.ui;

import java.util.Arrays;
import java.util.Optional;

import actas.AppContext;
import actas.model.Usuario;

/**
 * Inicio de sesión. En el original los campos no se validaban: "Aceptar" no tenía acción y la
 * clave se mostraba en texto plano.
 */
public class LoginDialog extends javax.swing.JDialog {

    private static final int MAX_INTENTOS = 3;

    private final AppContext ctx;
    private Usuario autenticado;
    private int intentos;

    public LoginDialog(java.awt.Frame parent, AppContext ctx) {
        super(parent, true);
        this.ctx = ctx;
        initComponents();
        setTitle("Sistema de Actas — Iniciar sesión");
        btnAceptar.addActionListener(evt -> ingresar());
        txtClave.addActionListener(evt -> ingresar());
        txtUsuario.addActionListener(evt -> txtClave.requestFocusInWindow());
        getRootPane().setDefaultButton(btnAceptar);
        setLocationRelativeTo(parent);
    }

    /** Muestra el diálogo y devuelve el usuario autenticado, o vacío si se canceló. */
    public Optional<Usuario> mostrar() {
        setVisible(true);
        return Optional.ofNullable(autenticado);
    }

    private void ingresar() {
        String usuario = txtUsuario.getText().trim();
        char[] clave = txtClave.getPassword();
        try {
            Ui.run(this, () -> {
                Optional<Usuario> u = ctx.usuarios().autenticar(usuario, clave);
                if (u.isPresent()) {
                    autenticado = u.get();
                    dispose();
                    return;
                }
                intentos++;
                txtClave.setText("");
                if (intentos >= MAX_INTENTOS) {
                    Ui.error(this, "Demasiados intentos fallidos.");
                    dispose();
                } else {
                    Ui.warn(this, "Usuario o clave incorrectos.");
                    txtClave.requestFocusInWindow();
                }
            });
        } finally {
            Arrays.fill(clave, '\0'); // no dejar la clave en memoria
        }
    }

    private void btnCancelarActionPerformed(java.awt.event.ActionEvent evt) {
        autenticado = null;
        dispose();
    }

    /** Construye el layout (código heredado del diseñador de NetBeans, sin lógica). */
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        txtUsuario = new javax.swing.JTextField();
        btnAceptar = new javax.swing.JButton();
        btnCancelar = new javax.swing.JButton();
        txtClave = new javax.swing.JPasswordField();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jLabel1.setText("Usuario");

        btnAceptar.setText("Aceptar");

        btnCancelar.setText("Cancelar");
        btnCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarActionPerformed(evt);
            }
        });

        jLabel2.setText("Clave");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addGap(27, 27, 27)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(txtClave, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(btnAceptar)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelar))
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, 251, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(54, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtUsuario, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtClave, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCancelar)
                    .addComponent(btnAceptar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    // Componentes Swing (layout heredado del diseñador de NetBeans)
    private javax.swing.JButton btnAceptar;
    private javax.swing.JButton btnCancelar;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPasswordField txtClave;
    private javax.swing.JTextField txtUsuario;
}
