package library.model;

/**
 * ABSTRACTION (interface): the contract for anything that can be lent out.
 * The rest of the system only needs to know these behaviours, not how a
 * specific item type implements them.
 */
public interface Borrowable {

    boolean isAvailable();

    void checkOut();

    void checkIn();

    /** Number of days the item may be kept before it is due. */
    int getLoanPeriodDays();

    /** Late fee for returning the item the given number of days after its due date. */
    double calculateLateFee(long daysLate);
}
