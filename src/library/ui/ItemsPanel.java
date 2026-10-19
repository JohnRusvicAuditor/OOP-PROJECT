package library.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import library.model.ItemType;
import library.model.LibraryItem;
import library.service.LibraryService;
import library.util.Money;

/** Tab for adding, searching, updating and deleting library items. */
public class ItemsPanel extends RefreshablePanel {

    private final DefaultTableModel model = readOnlyModel(
            "ID", "Type", "Title", "Author / Creator", "Year", "Details", "Loan Days", "Late Fee / Day", "Status");
    private final JTable table = createTable(model);

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> filterBox = new JComboBox<>();

    private final JComboBox<ItemType> typeBox = new JComboBox<>(ItemType.values());
    private final JTextField titleField = new JTextField(16);
    private final JTextField creatorField = new JTextField(14);
    private final JTextField yearField = new JTextField(5);
    private final JLabel extraLabel = new JLabel();
    private final JTextField extraField = new JTextField(12);

    public ItemsPanel(LibraryService service, Runnable onDataChanged) {
        super(service, onDataChanged);

        filterBox.addItem("All types");
        for (ItemType type : ItemType.values()) {
            filterBox.addItem(type.getLabel());
        }

        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);
        setColumnWidths(table, 50, 80, 230, 190, 55, 190, 80, 100, 80);

        typeBox.addActionListener(e -> updateExtraLabel());
        updateExtraLabel();
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedIntoForm();
            }
        });
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.add(new JLabel("Search (ID, title, author):"));
        bar.add(searchField);
        bar.add(new JLabel("Type:"));
        bar.add(filterBox);
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Show All");
        bar.add(searchButton);
        bar.add(clearButton);

        searchButton.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            filterBox.setSelectedIndex(0);
            refresh();
        });
        return bar;
    }

    private JPanel buildForm() {
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fields.add(new JLabel("Type:"));
        fields.add(typeBox);
        fields.add(new JLabel("Title:"));
        fields.add(titleField);
        fields.add(new JLabel("Author / Creator:"));
        fields.add(creatorField);
        fields.add(new JLabel("Year:"));
        fields.add(yearField);
        fields.add(extraLabel);
        fields.add(extraField);

        JButton addButton = new JButton("Add Item");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        addButton.addActionListener(e -> addItem());
        updateButton.addActionListener(e -> updateItem());
        deleteButton.addActionListener(e -> deleteItem());
        clearButton.addActionListener(e -> clearForm());

        JPanel form = new JPanel(new GridLayout(2, 1));
        form.setBorder(BorderFactory.createTitledBorder("Item details"));
        form.add(fields);
        form.add(buttons);
        return form;
    }

    private void updateExtraLabel() {
        ItemType type = (ItemType) typeBox.getSelectedItem();
        extraLabel.setText(type.getExtraLabel() + ":");
    }

    private ItemType selectedFilter() {
        int index = filterBox.getSelectedIndex();
        return index <= 0 ? null : ItemType.values()[index - 1];
    }

    private void addItem() {
        boolean ok = attempt(() -> {
            LibraryItem item = service.addItem((ItemType) typeBox.getSelectedItem(),
                    titleField.getText(), creatorField.getText(), yearField.getText(), extraField.getText());
            showInfo("Added " + item.getType().getLabel() + " " + item.getId() + ": " + item.getTitle());
        });
        if (ok) {
            clearForm();
            onDataChanged.run();
        }
    }

    private void updateItem() {
        String id = selectedValue(table, 0);
        if (id == null) {
            showError("Select an item in the table first.");
            return;
        }
        boolean ok = attempt(() -> {
            service.updateItem(id, titleField.getText(), creatorField.getText(),
                    yearField.getText(), extraField.getText());
            showInfo("Item " + id + " updated.");
        });
        if (ok) {
            onDataChanged.run();
        }
    }

    private void deleteItem() {
        String id = selectedValue(table, 0);
        if (id == null) {
            showError("Select an item in the table first.");
            return;
        }
        if (!confirm("Delete item " + id + "?")) {
            return;
        }
        boolean ok = attempt(() -> service.removeItem(id));
        if (ok) {
            clearForm();
            onDataChanged.run();
        }
    }

    private void loadSelectedIntoForm() {
        String id = selectedValue(table, 0);
        if (id == null) {
            return;
        }
        service.findItem(id).ifPresent(item -> {
            typeBox.setSelectedItem(item.getType());
            titleField.setText(item.getTitle());
            creatorField.setText(item.getCreator());
            yearField.setText(String.valueOf(item.getYear()));
            extraField.setText(item.getExtraValue());
        });
    }

    private void clearForm() {
        table.clearSelection();
        titleField.setText("");
        creatorField.setText("");
        yearField.setText("");
        extraField.setText("");
    }

    @Override
    public void refresh() {
        List<LibraryItem> found = service.searchItems(searchField.getText(), selectedFilter());
        model.setRowCount(0);
        for (LibraryItem item : found) {
            model.addRow(new Object[] {
                    item.getId(),
                    item.getType().getLabel(),
                    item.getTitle(),
                    item.getCreator(),
                    item.getYear(),
                    item.getExtraLabel() + ": " + item.getExtraValue(),
                    item.getLoanPeriodDays(),
                    Money.format(item.getLateFeePerDay()),
                    item.isAvailable() ? "Available" : "On loan"
            });
        }
    }
}
