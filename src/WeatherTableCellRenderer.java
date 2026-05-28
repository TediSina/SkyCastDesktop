import java.awt.Color;
import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Shared renderer for forecast and history tables.
 */
public class WeatherTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * Applies zebra striping and app-specific padding to a table cell.
     */
    @Override
    public Component getTableCellRendererComponent(
            JTable table,
            Object value,
            boolean isSelected,
            boolean hasFocus,
            int row,
            int column
    ) {
        Component component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (!isSelected) {
            component.setBackground(row % 2 == 0 ? Color.WHITE : new Color(244, 250, 251));
            component.setForeground(AppTheme.TEXT);
        }
        setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        return component;
    }
}
