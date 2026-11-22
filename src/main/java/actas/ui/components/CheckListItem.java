package actas.ui.components;

/** Elemento seleccionable de una lista con casillas de verificación. */
public class CheckListItem<T> {

    private final T value;
    private boolean selected;

    public CheckListItem(T value) {
        this.value = value;
    }

    public T value() {
        return value;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void toggle() {
        selected = !selected;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
