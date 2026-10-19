package library.util;

/** Small helper so every screen formats money the same way. */
public final class Money {

    private Money() {
    }

    public static String format(double amount) {
        return String.format("PHP %,.2f", amount);
    }
}
