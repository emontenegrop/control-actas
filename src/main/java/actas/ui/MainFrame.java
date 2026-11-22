package actas.ui;

import java.awt.event.KeyEvent;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import actas.AppContext;

/** Ventana principal MDI con el menú de la aplicación. */
public class MainFrame extends javax.swing.JFrame {

    private final AppContext ctx;

    public MainFrame(AppContext ctx) {
        this.ctx = ctx;
        initComponents();
        setTitle("Sistema de Actas — " + ctx.usuarioActual().usuario() + " (" + ctx.usuarioActual().tipoUsuario() + ")");
        setLocationByPlatform(true);
        mniReporteAsistencias.addActionListener(evt -> abrir(new AsistenciasReportFrame(ctx), false));
        agregarMenuSistema();
        // Solo los administradores gestionan usuarios
        mniUsuarios.setEnabled("Administrador".equalsIgnoreCase(ctx.usuarioActual().tipoUsuario()));
        mniProgramadas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        mniCerradas.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
    }

    private void agregarMenuSistema() {
        JMenu sistema = new JMenu("Sistema");
        JMenuItem acerca = new JMenuItem("Acerca de");
        acerca.addActionListener(evt -> Ui.info(this, """
                Sistema de Actas 2.0
                Gestión de comités, reuniones y actas.

                Java %s · Swing · MySQL""".formatted(Runtime.version().feature())));
        JMenuItem salir = new JMenuItem("Salir");
        salir.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        salir.addActionListener(evt -> dispatchEvent(new java.awt.event.WindowEvent(this, java.awt.event.WindowEvent.WINDOW_CLOSING)));
        sistema.add(acerca);
        sistema.addSeparator();
        sistema.add(salir);
        menuBar.add(sistema, 0);
    }

    private void abrir(JInternalFrame frame, boolean maximizado) {
        Ui.show(desktop, frame, maximizado);
    }

    private void mniProgramadasActionPerformed(java.awt.event.ActionEvent evt) {
        abrir(new SeleccionReunionFrame(ctx, reunion -> abrir(new RegistroActaFrame(ctx, reunion), true)), false);
    }

    private void mniCerradasActionPerformed(java.awt.event.ActionEvent evt) {
        abrir(new ReunionesCerradasFrame(ctx), true);
    }

    private void mnuMantenimientoActionPerformed(java.awt.event.ActionEvent evt) {
        // sin acción: es un menú contenedor
    }

    private void mniMiembrosActionPerformed(java.awt.event.ActionEvent evt) {
        abrir(new MiembrosFrame(ctx), false);
    }

    private void mniReunionesActionPerformed(java.awt.event.ActionEvent evt) {
        abrir(new ReunionesFrame(ctx), false);
    }

    private void mniUsuariosActionPerformed(java.awt.event.ActionEvent evt) {
        abrir(new UsuariosFrame(ctx), false);
    }

    private void mniReporteActasActionPerformed(java.awt.event.ActionEvent evt) {
        abrir(new ReunionesCerradasFrame(ctx), true);
    }

    /** Construye el layout (código heredado del diseñador de NetBeans, sin lógica). */
    private void initComponents() {

        desktop = new javax.swing.JDesktopPane();
        menuBar = new javax.swing.JMenuBar();
        mnuReuniones = new javax.swing.JMenu();
        mniProgramadas = new javax.swing.JMenuItem();
        mniCerradas = new javax.swing.JMenuItem();
        mnuMantenimiento = new javax.swing.JMenu();
        mniMiembros = new javax.swing.JMenuItem();
        mniReuniones = new javax.swing.JMenuItem();
        mniUsuarios = new javax.swing.JMenuItem();
        mnuReportes = new javax.swing.JMenu();
        mniReporteActas = new javax.swing.JMenuItem();
        mniReporteAsistencias = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Control de Actas");
        setName("Formulario");

        mnuReuniones.setText("Reuniones");

        mniProgramadas.setText("Programadas");
        mniProgramadas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniProgramadasActionPerformed(evt);
            }
        });
        mnuReuniones.add(mniProgramadas);

        mniCerradas.setText("Cerradas");
        mniCerradas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniCerradasActionPerformed(evt);
            }
        });
        mnuReuniones.add(mniCerradas);

        menuBar.add(mnuReuniones);

        mnuMantenimiento.setText("Mantenimiento");
        mnuMantenimiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuMantenimientoActionPerformed(evt);
            }
        });

        mniMiembros.setText("Miembros");
        mniMiembros.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniMiembrosActionPerformed(evt);
            }
        });
        mnuMantenimiento.add(mniMiembros);

        mniReuniones.setText("Reuniones");
        mniReuniones.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniReunionesActionPerformed(evt);
            }
        });
        mnuMantenimiento.add(mniReuniones);

        mniUsuarios.setText("Usuarios");
        mniUsuarios.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniUsuariosActionPerformed(evt);
            }
        });
        mnuMantenimiento.add(mniUsuarios);

        menuBar.add(mnuMantenimiento);

        mnuReportes.setText("Reportes");

        mniReporteActas.setText("Actas");
        mniReporteActas.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mniReporteActasActionPerformed(evt);
            }
        });
        mnuReportes.add(mniReporteActas);

        mniReporteAsistencias.setText("Asistencias");
        mnuReportes.add(mniReporteAsistencias);

        menuBar.add(mnuReportes);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 786, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(desktop, javax.swing.GroupLayout.DEFAULT_SIZE, 604, Short.MAX_VALUE)
        );

        pack();
    }

    // Componentes Swing (layout heredado del diseñador de NetBeans)
    private javax.swing.JDesktopPane desktop;
    private javax.swing.JMenu mnuReuniones;
    private javax.swing.JMenu mnuMantenimiento;
    private javax.swing.JMenu mnuReportes;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem mniProgramadas;
    private javax.swing.JMenuItem mniUsuarios;
    private javax.swing.JMenuItem mniMiembros;
    private javax.swing.JMenuItem mniReuniones;
    private javax.swing.JMenuItem mniCerradas;
    private javax.swing.JMenuItem mniReporteActas;
    private javax.swing.JMenuItem mniReporteAsistencias;
}
