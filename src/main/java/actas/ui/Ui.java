package actas.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import actas.db.DataAccessException;
import actas.model.Opcion;
import actas.service.ValidationException;

/** Utilidades comunes de la interfaz: mensajes, manejo de errores y combos. */
public final class Ui {

    private static final Logger LOG = Logger.getLogger(Ui.class.getName());

    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }

    private Ui() {
    }

    /**
     * Ejecuta una acción mostrando cualquier error al usuario. Reemplaza los
     * {@code catch (Exception e) {}} vacíos del original, que ocultaban todos los fallos.
     */
    public static boolean run(Component parent, Action action) {
        try {
            action.run();
            return true;
        } catch (ValidationException e) {
            warn(parent, "Revise los datos ingresados:\n\n• " + String.join("\n• ", e.errores()));
        } catch (DataAccessException.IntegrityViolationException e) {
            LOG.log(Level.INFO, "Restricción de integridad", e);
            warn(parent, "No se puede completar la operación: el registro está duplicado o es "
                    + "utilizado por otros datos (por ejemplo, actas ya registradas).");
        } catch (IllegalStateException | IllegalArgumentException e) {
            warn(parent, e.getMessage());
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error inesperado", e);
            error(parent, "Ocurrió un error inesperado:\n" + e.getMessage());
        }
        return false;
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Sistema de Actas", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Sistema de Actas", JOptionPane.WARNING_MESSAGE);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Sistema de Actas", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirmar", JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static <T> void fill(JComboBox<T> combo, List<T> items) {
        combo.setModel(new DefaultComboBoxModel<>(new java.util.Vector<>(items)));
    }

    /** Selecciona en el combo la opción con el id indicado. */
    public static void selectById(JComboBox<Opcion> combo, Integer id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (id != null && combo.getItemAt(i).id() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
        combo.setSelectedIndex(-1);
    }

    public static Opcion selected(JComboBox<Opcion> combo) {
        return (Opcion) combo.getSelectedItem();
    }

    /** Agrega el formulario al escritorio MDI, centrado, y lo muestra. */
    public static void show(JDesktopPane desktop, JInternalFrame frame, boolean maximized) {
        desktop.add(frame);
        Dimension d = desktop.getSize();
        Dimension f = frame.getSize();
        frame.setLocation(Math.max(0, (d.width - f.width) / 2), Math.max(0, (d.height - f.height) / 2));
        frame.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            try {
                frame.setSelected(true);
                if (maximized) {
                    frame.setMaximum(true);
                }
            } catch (java.beans.PropertyVetoException ignored) {
                // el usuario o el gestor de ventanas lo impidió; no es un error
            }
        });
    }
}
