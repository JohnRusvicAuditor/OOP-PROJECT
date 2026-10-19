package library.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import library.model.Member;
import library.model.MemberType;
import library.service.LibraryService;

/** Tab for registering, searching and removing library members. */
public class MembersPanel extends RefreshablePanel {

    private final DefaultTableModel model = readOnlyModel(
            "ID", "Type", "Name", "Email", "Max Loans", "Extra Loan Days", "Fee Discount", "Items Borrowed");
    private final JTable table = createTable(model);

    private final JTextField searchField = new JTextField(22);
    private final JComboBox<MemberType> typeBox = new JComboBox<>(MemberType.values());
    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(22);

    public MembersPanel(LibraryService service, Runnable onDataChanged) {
        super(service, onDataChanged);
        add(buildSearchBar(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildForm(), BorderLayout.SOUTH);
    }

    private JPanel buildSearchBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.add(new JLabel("Search (ID, name, email):"));
        bar.add(searchField);
        JButton searchButton = new JButton("Search");
        JButton clearButton = new JButton("Show All");
        bar.add(searchButton);
        bar.add(clearButton);

        searchButton.addActionListener(e -> refresh());
        searchField.addActionListener(e -> refresh());
        clearButton.addActionListener(e -> {
            searchField.setText("");
            refresh();
        });
        return bar;
    }

    private JPanel buildForm() {
        JPanel fields = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        fields.add(new JLabel("Type:"));
        fields.add(typeBox);
        fields.add(new JLabel("Name:"));
        fields.add(nameField);
        fields.add(new JLabel("Email:"));
        fields.add(emailField);

        JButton addButton = new JButton("Register Member");
        JButton deleteButton = new JButton("Delete Selected");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        buttons.add(addButton);
        buttons.add(deleteButton);

        addButton.addActionListener(e -> addMember());
        deleteButton.addActionListener(e -> deleteMember());

        JPanel form = new JPanel(new GridLayout(2, 1));
        form.setBorder(BorderFactory.createTitledBorder("Member details"));
        form.add(fields);
        form.add(buttons);
        return form;
    }

    private void addMember() {
        boolean ok = attempt(() -> {
            Member member = service.addMember((MemberType) typeBox.getSelectedItem(),
                    nameField.getText(), emailField.getText());
            showInfo("Registered " + member.getType().getLabel() + " " + member.getId() + ": " + member.getName());
        });
        if (ok) {
            nameField.setText("");
            emailField.setText("");
            onDataChanged.run();
        }
    }

    private void deleteMember() {
        String id = selectedValue(table, 0);
        if (id == null) {
            showError("Select a member in the table first.");
            return;
        }
        if (!confirm("Delete member " + id + "?")) {
            return;
        }
        if (attempt(() -> service.removeMember(id))) {
            onDataChanged.run();
        }
    }

    @Override
    public void refresh() {
        model.setRowCount(0);
        for (Member m : service.searchMembers(searchField.getText())) {
            model.addRow(new Object[] {
                    m.getId(),
                    m.getType().getLabel(),
                    m.getName(),
                    m.getEmail(),
                    m.getMaxLoans(),
                    m.getExtraLoanDays(),
                    Math.round(m.getFeeDiscount() * 100) + "%",
                    service.countActiveLoans(m)
            });
        }
    }
}
