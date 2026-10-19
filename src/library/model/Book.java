package library.model;

import library.util.Validator;

/** INHERITANCE: a Book is a LibraryItem. 14-day loan, PHP 5 per day late. */
public class Book extends LibraryItem {

    private static final int LOAN_DAYS = 14;
    private static final double FEE_PER_DAY = 5.0;

    private String isbn = "";

    public Book(String id, String title, String author, int year) {
        super(id, title, author, year);
    }

    @Override
    public ItemType getType() {
        return ItemType.BOOK;
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
        return isbn;
    }

    @Override
    public void setExtraValue(String value) {
        String cleaned = Validator.requireText("ISBN", value, 20).replace("-", "").replace(" ", "");
        if (!cleaned.matches("\\d{9}[\\dXx]|\\d{13}")) {
            throw new IllegalArgumentException("ISBN must contain 10 or 13 digits.");
        }
        this.isbn = cleaned.toUpperCase();
    }
}
