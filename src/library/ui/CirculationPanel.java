package library.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import library.model.LibraryItem;
import library.model.Loan;
import library.model.Member;
import library.service.LibraryService;
import library.util.Money;
import library.util.Validator;

/** Tab for borrowing and returning items (the main transaction screen). */
public class CirculationPanel extends RefreshablePanel {

    private final DefaultComboBoxModel<LibraryItem> itemModel = new DefaultComboBoxModel<>();
    private final DefaultComboBoxModel<Member> memberModel = new DefaultComboBoxModel<>();
    private final JComboBox<LibraryItem> itemBox = new JComboBox<>(itemModel);
    private final JComboBox<Member> memberBox = new JComboBox<>(memberModel);
    private final JTextField borrowDateField = new JTextField(LocalDate.now().toString(), 9);

    private final DefaultTableModel loanModel = readOnlyModel(
            "Loan ID", "Item", "Borrower", "Borrowed", "Due", "Status");
    private final JTable loanTable = createTable(loanModel);
    private final JTextField returnDateField = new JTextField(LocalDate.now().toString(), 9);

    public CirculationPanel(LibraryService service, Runnable onDataChanged) {
        super(service, onDataChanged);
        itemBox.setPrototypeDisplayValue(null);
        add(buildBorrowPanel(), BorderLayout.NORTH);
        add(new JScrollPane(loanTable), BorderLayout.CENTER);
        add(buildReturnPanel(), BorderLayout.SOUTH);
    }

    private JPanel buildBorrowPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Borrow an item"));
        panel.add(new JLabel("Available item:"));
        panel.add(itemBox);
        panel.add(new JLabel("Member:"));
        panel.add(memberBox);
        panel.add(new JLabel("Borrow date (yyyy-MM-dd):"));
        panel.add(borrowDateField);
        JButton borrowButton = new JButton("Borrow");
        panel.add(borrowButton);
        borrowButton.addActionListener(e -> borrow());
        return panel;
    }

    private JPanel buildReturnPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        panel.setBorder(BorderFactory.createTitledBorder("Return the selected loan"));
        panel.add(new JLabel("Return date (yyyy-MM-dd):"));
        panel.add(returnDateField);
        JButton returnButton = new JButton("Return Selected Item");
        panel.add(returnButton);
        returnButton.addActionListener(e -> returnSelected());
        return panel;
    }

    private void borrow() {
        LibraryItem item = (LibraryItem) itemBox.getSelectedItem();
        Member member = (Member) memberBox.getSelectedItem();
        if (item == null) {
            showError("There is no available item to borrow.");
            return;
        }
        if (member == null) {
            showError("Register a member first.");
            return;
        }
        boolean ok = attempt(() -> {
            LocalDate date = Validator.parseDate("Borrow date", borrowDateField.getText());
            Loan loan = service.borrowItem(item.getId(), member.getId(), date);
            showInfo(member.getName() + " borrowed \"" + item.getTitle() + "\".\nDue date: " + loan.getDueDate());
        });
        if (ok) {
            onDataChanged.run();
        }
    }

    private void returnSelected() {
        String idText = selectedValue(loanTable, 0);
        if (idText == null) {
            showError("Select a loan in the table first.");
            return;
        }
        boolean ok = attempt(() -> {
            LocalDate date = Validator.parseDate("Return date", returnDateField.getText());
            Loan loan = service.returnItem(Integer.parseInt(idText), date);
            long late = loan.getDaysLate(date);
            String message = "\"" + loan.getItem().getTitle() + "\" returned.\n";
            message += late > 0
                    ? "Days late: " + late + "\nLate fee: " + Money.format(loan.getFeeCharged())
                    : "Returned on time. No fee.";
            showInfo(message);
        });
        if (ok) {
            onDataChanged.run();
        }
    }

    @Override
    public void refresh() {
        Object selectedItem = itemBox.getSelectedItem();
        Object selectedMember = memberBox.getSelectedItem();

        itemModel.removeAllElements();
        for (LibraryItem item : service.getAvailableItems()) {
            itemModel.addElement(item);
        }
        memberModel.removeAllElements();
        for (Member member : service.searchMembers("")) {
            memberModel.addElement(member);
        }
        if (selectedItem != null && itemModel.getIndexOf(selectedItem) >= 0) {
            itemBox.setSelectedItem(selectedItem);
        }
        if (selectedMember != null && memberModel.getIndexOf(selectedMember) >= 0) {
            memberBox.setSelectedItem(selectedMember);
        }

        LocalDate today = LocalDate.now();
        loanModel.setRowCount(0);
        for (Loan loan : service.getActiveLoans()) {
            loanModel.addRow(new Object[] {
                    loan.getId(),
                    loan.getItem().toString(),
                    loan.getMember().toString(),
                    loan.getBorrowDate(),
                    loan.getDueDate(),
                    loan.isOverdue(today) ? "OVERDUE (" + loan.getDaysLate(today) + " days)" : "On time"
            });
        }
    }
}
