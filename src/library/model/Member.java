package library.model;

import library.util.Validator;

/**
 * ABSTRACTION + INHERITANCE: common data and rules for every library member.
 * Borrowing privileges differ per member type, so Student and Faculty supply
 * their own limits, extra loan days and fee discount.
 *
 * ENCAPSULATION: private fields, validated setters, no setter for the id.
 */
public abstract class Member {

    private final String id;
    private String name;
    private String email;

    protected Member(String id, String name, String email) {
        this.id = id;
        setName(name);
        setEmail(email);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public final void setName(String name) {
        this.name = Validator.requireName("Name", name);
    }

    public final void setEmail(String email) {
        this.email = Validator.requireEmail(email);
    }

    // ---------- abstract privileges: defined by each member type ----------

    public abstract MemberType getType();

    /** Maximum number of items this member may borrow at the same time. */
    public abstract int getMaxLoans();

    /** Extra days added on top of the item's own loan period. */
    public abstract int getExtraLoanDays();

    /** Fraction (0.0 - 1.0) taken off any late fee. */
    public abstract double getFeeDiscount();

    /** Applies this member's discount to a late fee. */
    public double applyDiscount(double fee) {
        return fee * (1.0 - getFeeDiscount());
    }

    @Override
    public String toString() {
        return id + " - " + name;
    }
}
