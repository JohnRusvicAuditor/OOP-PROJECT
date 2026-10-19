package library.model;

import library.util.Validator;

/**
 * INHERITANCE: a DVD is a LibraryItem. 3-day loan, PHP 20 per day late.
 * POLYMORPHISM: overrides calculateLateFee to cap the fee at PHP 500.
 */
public class DVD extends LibraryItem {

    private static final int LOAN_DAYS = 3;
    private static final double FEE_PER_DAY = 20.0;
    private static final double MAX_FEE = 500.0;

    private int durationMinutes = 1;

    public DVD(String id, String title, String director, int year) {
        super(id, title, director, year);
    }

    @Override
    public ItemType getType() {
        return ItemType.DVD;
    }

    @Override
    public int getLoanPeriodDays() {
        return LOAN_DAYS;
    }

    @Override
    public double getLateFeePerDay() {
        return FEE_PER_DAY;
    }

    @Override
    public double calculateLateFee(long daysLate) {
        return Math.min(super.calculateLateFee(daysLate), MAX_FEE);
    }

    @Override
    public String getExtraValue() {
        return String.valueOf(durationMinutes);
    }

    @Override
    public void setExtraValue(String value) {
        this.durationMinutes = Validator.parseInt("Duration (minutes)", value, 1, 600);
    }
}
