package library.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import library.service.LibraryException;
import library.service.LibraryService;

/**
 * ABSTRACTION + INHERITANCE for the GUI: every tab is a RefreshablePanel.
 * The base class provides shared behaviour (error dialogs, table setup) and
 * forces each tab to say how it reloads its data from the service.
 *
 * POLYMORPHISM: MainFrame calls refresh() on any RefreshablePanel and each
 * tab reloads itself in its own way.
 */
public abstract class RefreshablePanel extends JPanel {

    protected final LibraryService service;
    protected final Runnable onDataChanged;

    protected RefreshablePanel(LibraryService service, Runnable onDataChanged) {
        super(new BorderLayout(8, 8));
        this.service = service;
        this.onDataChanged = onDataChanged;
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    }

    /** Reload everything shown on this tab from the service. */
    public abstract void refresh();

    /** A piece of work that may fail with a business-rule or validation error. */
    @FunctionalInterface
    protected interface Action {
        void run() throws LibraryException;
    }

    /**
     * Runs an action and turns any validation / business-rule error into a
     * friendly dialog. Returns true when the action succeeded.
     */
    protected boolean attempt(Action action) {
        try {
            action.run();
            return true;
        } catch (LibraryException | IllegalArgumentException | IllegalStateException ex) {
            showError(ex.getMessage());
            return false;
        }
    }

    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Cannot complete action", JOptionPane.ERROR_MESSAGE);
    }

    protected void showInfo(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean confirm(String message) {
        return JOptionPane.showConfirmDialog(this, message, "Please confirm",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    protected static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    protected static JTable createTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setRowHeight(24);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);
        return table;
    }

    /** Sets the preferred width of each column, in order. */
    protected static void setColumnWidths(JTable table, int... widths) {
        for (int i = 0; i < widths.length && i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    /** Value in the given column of the selected row, or null when nothing is selected. */
    protected static String selectedValue(JTable table, int column) {
        int row = table.getSelectedRow();
        if (row < 0) {
            return null;
        }
        int modelRow = table.convertRowIndexToModel(row);
        return String.valueOf(table.getModel().getValueAt(modelRow, column));
    }
}
