package library.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Records one borrowing transaction. The due date and the fee come from the
 * item and member objects, so each loan automatically follows the rules of the
 * specific item type and member type involved (POLYMORPHISM).
 */
public class Loan {

    private final int id;
    private final LibraryItem item;
    private final Member member;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private double feeCharged;

    public Loan(int id, LibraryItem item, Member member, LocalDate borrowDate) {
        this.id = id;
        this.item = item;
        this.member = member;
        this.borrowDate = borrowDate;
        this.dueDate = borrowDate.plusDays(item.getLoanPeriodDays() + member.getExtraLoanDays());
    }

    public int getId() {
        return id;
    }

    public LibraryItem getItem() {
        return item;
    }

    public Member getMember() {
        return member;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public double getFeeCharged() {
        return feeCharged;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    /** Days past the due date, measured to the return date (if returned) or to asOf. */
    public long getDaysLate(LocalDate asOf) {
        LocalDate end = isReturned() ? returnDate : asOf;
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }

    public boolean isOverdue(LocalDate asOf) {
        return !isReturned() && asOf.isAfter(dueDate);
    }

    /** Closes the loan and computes the fee using the item's and member's own rules. */
    public void markReturned(LocalDate date) {
        this.returnDate = date;
        this.feeCharged = member.applyDiscount(item.calculateLateFee(getDaysLate(date)));
    }
}
