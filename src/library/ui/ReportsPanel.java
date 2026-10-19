package library.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import library.model.Loan;
import library.service.LibraryService;
import library.util.Money;
import library.util.Validator;

/** Tab with library statistics, the overdue report and the full loan history. */
public class ReportsPanel extends RefreshablePanel {

    private final JLabel totalItems = statValue();
    private final JLabel availableItems = statValue();
    private final JLabel members = statValue();
    private final JLabel activeLoans = statValue();
    private final JLabel overdueLoans = statValue();
    private final JLabel feesCollected = statValue();

    private final JTextField asOfField = new JTextField(LocalDate.now().toString(), 9);
    private final DefaultTableModel overdueModel = readOnlyModel(
            "Loan ID", "Item", "Borrower", "Due", "Days Late", "Fee If Returned Today");
    private final DefaultTableModel historyModel = readOnlyModel(
            "Loan ID", "Item", "Borrower", "Borrowed", "Due", "Returned", "Fee Charged");

    public ReportsPanel(LibraryService service, Runnable onDataChanged) {
        super(service, onDataChanged);
        add(buildStatsBar(), BorderLayout.NORTH);

        JPanel overduePanel = new JPanel(new BorderLayout(4, 4));
        overduePanel.setBorder(BorderFactory.createTitledBorder("Overdue report"));
        overduePanel.add(buildAsOfBar(), BorderLayout.NORTH);
        overduePanel.add(new JScrollPane(createTable(overdueModel)), BorderLayout.CENTER);

        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBorder(BorderFactory.createTitledBorder("Loan history"));
        historyPanel.add(new JScrollPane(createTable(historyModel)), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, overduePanel, historyPanel);
        split.setResizeWeight(0.5);
        add(split, BorderLayout.CENTER);
    }

    private static JLabel statValue() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(label.getFont().deriveFont(Font.BOLD, 22f));
        return label;
    }

    private JPanel buildStatsBar() {
        JPanel bar = new JPanel(new GridLayout(1, 6, 8, 8));
        bar.add(card("Total items", totalItems));
        bar.add(card("Available", availableItems));
        bar.add(card("Members", members));
        bar.add(card("Active loans", activeLoans));
        bar.add(card("Overdue", overdueLoans));
        bar.add(card("Fees collected", feesCollected));
        return bar;
    }

    private JPanel card(String title, JLabel value) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));
        JLabel caption = new JLabel(title, SwingConstants.CENTER);
        caption.setForeground(Color.GRAY);
        card.add(caption, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        return card;
    }

    private JPanel buildAsOfBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        bar.add(new JLabel("Show overdue as of (yyyy-MM-dd):"));
        bar.add(asOfField);
        JButton button = new JButton("Update Report");
        bar.add(button);
        button.addActionListener(e -> {
            if (attempt(() -> Validator.parseDate("Report date", asOfField.getText()))) {
                refresh();
            }
        });
        return bar;
    }

    @Override
    public void refresh() {
        LocalDate asOf;
        try {
            asOf = Validator.parseDate("Report date", asOfField.getText());
        } catch (IllegalArgumentException ex) {
            asOf = LocalDate.now();
            asOfField.setText(asOf.toString());
        }

        totalItems.setText(String.valueOf(service.getTotalItems()));
        availableItems.setText(String.valueOf(service.getAvailableItemCount()));
        members.setText(String.valueOf(service.getTotalMembers()));
        activeLoans.setText(String.valueOf(service.getActiveLoans().size()));
        overdueLoans.setText(String.valueOf(service.getOverdueLoans(asOf).size()));
        feesCollected.setText(Money.format(service.getTotalFeesCollected()));

        overdueModel.setRowCount(0);
        for (Loan loan : service.getOverdueLoans(asOf)) {
            long late = loan.getDaysLate(asOf);
            double fee = loan.getMember().applyDiscount(loan.getItem().calculateLateFee(late));
            overdueModel.addRow(new Object[] {
                    loan.getId(), loan.getItem().toString(), loan.getMember().toString(),
                    loan.getDueDate(), late, Money.format(fee)
            });
        }

        historyModel.setRowCount(0);
        for (Loan loan : service.getAllLoans()) {
            historyModel.addRow(new Object[] {
                    loan.getId(), loan.getItem().toString(), loan.getMember().toString(),
                    loan.getBorrowDate(), loan.getDueDate(),
                    loan.isReturned() ? loan.getReturnDate().toString() : "-",
                    loan.isReturned() ? Money.format(loan.getFeeCharged()) : "-"
            });
        }
    }
}
