package library.model;

import library.util.Validator;

/** INHERITANCE: a Magazine is a LibraryItem. 7-day loan, PHP 3 per day late. */
public class Magazine extends LibraryItem {

    private static final int LOAN_DAYS = 7;
    private static final double FEE_PER_DAY = 3.0;

    private int issueNumber = 1;

    public Magazine(String id, String title, String publisher, int year) {
        super(id, title, publisher, year);
    }

    @Override
    public ItemType getType() {
        return ItemType.MAGAZINE;
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
    public String getExtraValue() {
        return String.valueOf(issueNumber);
    }

    @Override
    public void setExtraValue(String value) {
        this.issueNumber = Validator.parseInt("Issue number", value, 1, 9999);
    }
}
