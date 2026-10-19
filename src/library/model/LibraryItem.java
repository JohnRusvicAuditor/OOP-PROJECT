package library.model;

import java.time.LocalDate;

import library.util.Validator;

/**
 * ABSTRACTION (abstract class) + INHERITANCE (parent of Book, Magazine, DVD).
 *
 * Holds what every library item has in common. Anything that differs per item
 * type (loan period, fee rate, the extra field) is declared abstract and must be
 * supplied by the child classes.
 *
 * ENCAPSULATION: all fields are private; they are only changed through
 * setters that validate the new value first.
 */
public abstract class LibraryItem implements Borrowable {

    private final String id;
    private String title;
    private String creator;
    private int year;
    private boolean available = true;

    protected LibraryItem(String id, String title, String creator, int year) {
        this.id = id;
        setTitle(title);
        setCreator(creator);
        setYear(year);
    }

    // ---------- getters ----------

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCreator() {
        return creator;
    }

    public int getYear() {
        return year;
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    // ---------- validated setters (final so constructors can safely call them) ----------

    public final void setTitle(String title) {
        this.title = Validator.requireText("Title", title, 100);
    }

    public final void setCreator(String creator) {
        this.creator = Validator.requireText("Author / Creator", creator, 80);
    }

    public final void setYear(int year) {
        Validator.requireRange("Year", year, 1000, LocalDate.now().getYear() + 1);
        this.year = year;
    }

    // ---------- Borrowable behaviour shared by all items ----------

    @Override
    public void checkOut() {
        if (!available) {
            throw new IllegalStateException(title + " is already checked out.");
        }
        available = false;
    }

    @Override
    public void checkIn() {
        if (available) {
            throw new IllegalStateException(title + " is not currently checked out.");
        }
        available = true;
    }

    /** Default fee rule: days late multiplied by the daily rate. Children may override. */
    @Override
    public double calculateLateFee(long daysLate) {
        return daysLate <= 0 ? 0.0 : daysLate * getLateFeePerDay();
    }

    // ---------- abstract: each child class must define these ----------

    public abstract ItemType getType();

    public abstract double getLateFeePerDay();

    /** Value of the type-specific field (ISBN / issue number / duration). */
    public abstract String getExtraValue();

    /** Sets and validates the type-specific field. */
    public abstract void setExtraValue(String value);

    public String getExtraLabel() {
        return getType().getExtraLabel();
    }

    @Override
    public String toString() {
        return id + " - " + title;
    }
}
