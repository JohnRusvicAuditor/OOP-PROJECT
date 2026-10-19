package library.model;

/** INHERITANCE: faculty may borrow 10 items, get 7 extra days and a 50% fee discount. */
public class Faculty extends Member {

    public Faculty(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public MemberType getType() {
        return MemberType.FACULTY;
    }

    @Override
    public int getMaxLoans() {
        return 10;
    }

    @Override
    public int getExtraLoanDays() {
        return 7;
    }

    @Override
    public double getFeeDiscount() {
        return 0.5;
    }
}
