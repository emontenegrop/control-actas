package actas.ui.components;

import java.awt.Component;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

/** Dibuja cada {@link CheckListItem} como una casilla de verificación. */
public class CheckListRenderer extends JCheckBox implements ListCellRenderer<CheckListItem<?>> {

    @Override
    public Component getListCellRendererComponent(JList<? extends CheckListItem<?>> list, CheckListItem<?> value,
                                                  int index, boolean isSelected, boolean cellHasFocus) {
        setEnabled(list.isEnabled());
        setSelected(value.isSelected());
        setFont(list.getFont());
        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        setText(value.toString());
        setBorder(cellHasFocus ? UIManager.getBorder("List.focusCellHighlightBorder") : null);
        setBorderPainted(cellHasFocus);
        return this;
    }
}
