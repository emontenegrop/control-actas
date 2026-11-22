package actas.ui.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.swing.table.AbstractTableModel;

/**
 * Modelo de tabla de solo lectura respaldado por una lista de objetos del dominio.
 *
 * <p>El original ({@code ModeloTabla}) ejecutaba {@code SELECT *} dentro del constructor y, para
 * saber qué fila se había pulsado, volvía a consultar toda la tabla y leía columnas por posición.
 * Aquí cada fila es directamente el objeto ({@link #getRow(int)}).
 */
public class ListTableModel<T> extends AbstractTableModel {

    public record Column<T>(String title, Function<T, ?> value) {
    }

    private final List<Column<T>> columns;
    private List<T> rows = new ArrayList<>();

    @SafeVarargs
    public ListTableModel(Column<T>... columns) {
        this.columns = List.of(columns);
    }

    public static <T> Column<T> col(String title, Function<T, ?> value) {
        return new Column<>(title, value);
    }

    public void setRows(List<T> newRows) {
        this.rows = new ArrayList<>(newRows);
        fireTableDataChanged();
    }

    public T getRow(int index) {
        return rows.get(index);
    }

    public Optional<T> rowAt(int index) {
        return index >= 0 && index < rows.size() ? Optional.of(rows.get(index)) : Optional.empty();
    }

    public int indexOf(T row) {
        return rows.indexOf(row);
    }

    @Override
    public int getRowCount() {
        return rows.size();
    }

    @Override
    public int getColumnCount() {
        return columns.size();
    }

    @Override
    public String getColumnName(int column) {
        return columns.get(column).title();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = columns.get(columnIndex).value().apply(rows.get(rowIndex));
        return value == null ? "" : value;
    }
}
