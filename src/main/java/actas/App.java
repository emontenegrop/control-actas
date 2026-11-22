package actas;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

import com.formdev.flatlaf.FlatLightLaf;

import actas.config.DatabaseConfig;
import actas.db.Jdbc;
import actas.ui.LoginDialog;
import actas.ui.MainFrame;
import actas.ui.Ui;

/** Punto de entrada de la aplicación. */
public final class App {

    private static final Logger LOG = Logger.getLogger(App.class.getName());

    private App() {
    }

    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(App::iniciar);
    }

    private static void iniciar() {
        DatabaseConfig config = DatabaseConfig.load();
        LOG.info(() -> "Conectando con " + config);
        Jdbc jdbc = Jdbc.fromConfig(config);
        try {
            jdbc.ping();
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "No se pudo conectar a la base de datos", e);
            Ui.error(null, """
                    No se pudo conectar a la base de datos.

                    URL: %s
                    Detalle: %s

                    ¿Está levantado el contenedor?  docker compose up -d
                    """.formatted(config.url(), rootMessage(e)));
            System.exit(1);
            return;
        }

        AppContext ctx = new AppContext(jdbc);
        new LoginDialog(null, ctx).mostrar().ifPresentOrElse(usuario -> {
            ctx.setUsuarioActual(usuario);
            MainFrame frame = new MainFrame(ctx);
            frame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
            frame.setVisible(true);
        }, () -> System.exit(0));
    }

    private static String rootMessage(Throwable e) {
        Throwable t = e;
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t.getMessage();
    }
}
