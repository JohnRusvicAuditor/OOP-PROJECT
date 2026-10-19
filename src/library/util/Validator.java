package library.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Central place for input validation. Every method either returns a cleaned
 * value or throws an IllegalArgumentException with a user-friendly message.
 */
public final class Validator {

    private static final Pattern EMAIL =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PERSON_NAME =
            Pattern.compile("^[\\p{L}][\\p{L} .'-]*$");

    private Validator() {
        // utility class - not meant to be instantiated
    }

    /** Required, non-blank text with a maximum length. Returns the trimmed text. */
    public static String requireText(String field, String value, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required.");
        }
        String trimmed = value.trim();
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(field + " must be at most " + maxLength + " characters.");
        }
        return trimmed;
    }

    public static String requireName(String field, String value) {
        String trimmed = requireText(field, value, 60);
        if (!PERSON_NAME.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(
                    field + " may contain only letters, spaces, dots, apostrophes and hyphens.");
        }
        return trimmed;
    }

    public static String requireEmail(String value) {
        String trimmed = requireText("Email", value, 80);
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Email is not valid (example: name@example.com).");
        }
        return trimmed.toLowerCase();
    }

    public static void requireRange(String field, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(field + " must be between " + min + " and " + max + ".");
        }
    }

    /** Parses a whole number from text and checks that it is inside [min, max]. */
    public static int parseInt(String field, String value, int min, int max) {
        String trimmed = requireText(field, value, 20);
        int number;
        try {
            number = Integer.parseInt(trimmed);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(field + " must be a whole number.");
        }
        requireRange(field, number, min, max);
        return number;
    }

    /** Parses a date written as yyyy-MM-dd. */
    public static LocalDate parseDate(String field, String value) {
        String trimmed = requireText(field, value, 10);
        try {
            return LocalDate.parse(trimmed);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(field + " must use the format yyyy-MM-dd (e.g. 2026-10-19).");
        }
    }
}
