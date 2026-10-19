package library.model;

/** INHERITANCE: students may borrow 3 items, no extra days, no fee discount. */
public class Student extends Member {

    public Student(String id, String name, String email) {
        super(id, name, email);
    }

    @Override
    public MemberType getType() {
        return MemberType.STUDENT;
    }

    @Override
    public int getMaxLoans() {
        return 3;
    }

    @Override
    public int getExtraLoanDays() {
        return 0;
    }

    @Override
    public double getFeeDiscount() {
        return 0.0;
    }
}
