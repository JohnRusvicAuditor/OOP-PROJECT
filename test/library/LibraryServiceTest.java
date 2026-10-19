package library;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.util.List;

import library.model.Book;
import library.model.Borrowable;
import library.model.DVD;
import library.model.Faculty;
import library.model.ItemType;
import library.model.LibraryItem;
import library.model.Loan;
import library.model.Magazine;
import library.model.Member;
import library.model.MemberType;
import library.model.Student;
import library.service.LibraryException;
import library.service.LibraryService;

/**
 * Plain-Java test runner (no external libraries needed).
 * Run it with: java -cp out library.LibraryServiceTest
 */
public class LibraryServiceTest {

    private static int passed = 0;
    private static int failed = 0;

    private interface Check {
        void run() throws Exception;
    }

    private static void test(String name, Check check) {
        try {
            check.run();
            passed++;
            System.out.println("PASS  " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("FAIL  " + name + "  -> " + t);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertEquals(Object expected, Object actual) {
        if (!expected.equals(actual)) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    private static void assertMoney(double expected, double actual) {
        if (Math.abs(expected - actual) > 0.001) {
            throw new AssertionError("expected <" + expected + "> but was <" + actual + ">");
        }
    }

    /** Runs code that must be rejected with the given exception type. */
    private static void assertRejected(Class<? extends Throwable> type, Check check) throws Exception {
        try {
            check.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return;
            }
            throw new AssertionError("expected " + type.getSimpleName() + " but got " + t);
        }
        throw new AssertionError("expected " + type.getSimpleName() + " but nothing was thrown");
    }

    private static LibraryService fresh() throws LibraryException {
        LibraryService s = new LibraryService();
        s.addItem(ItemType.BOOK, "Clean Code", "Robert C. Martin", "2008", "9780132350884");     // B001
        s.addItem(ItemType.MAGAZINE, "TIME", "TIME USA", "2026", "12");                          // M001
        s.addItem(ItemType.DVD, "Planet Earth", "BBC Earth", "2006", "550");                      // D001
        s.addMember(MemberType.STUDENT, "Maria Santos", "maria@example.com");                    // S001
        s.addMember(MemberType.FACULTY, "Dr. Ana Reyes", "ana@example.com");                    // F001
        return s;
    }

    public static void main(String[] args) {
        LocalDate today = LocalDate.now();

        // ---------------- features ----------------
        test("Add item: IDs are generated per type (B001, M001, D001)", () -> {
            LibraryService s = fresh();
            assertEquals("B001", s.getItem("B001").getId());
            assertEquals("M001", s.getItem("M001").getId());
            assertEquals("D001", s.getItem("D001").getId());
            assertEquals("B002", s.addItem(ItemType.BOOK, "Another", "Author", "2000", "1234567890").getId());
        });

        test("Search items by title, author, ID and type", () -> {
            LibraryService s = fresh();
            assertEquals(1, s.searchItems("clean", null).size());
            assertEquals(1, s.searchItems("martin", null).size());
            assertEquals(1, s.searchItems("d001", null).size());
            assertEquals(3, s.searchItems("", null).size());
            assertEquals(1, s.searchItems("", ItemType.DVD).size());
            assertEquals(0, s.searchItems("zzz", null).size());
        });

        test("Update item changes values", () -> {
            LibraryService s = fresh();
            s.updateItem("B001", "Clean Code 2nd", "R. Martin", "2020", "9780132350884");
            assertEquals("Clean Code 2nd", s.getItem("B001").getTitle());
            assertEquals(2020, s.getItem("B001").getYear());
        });

        test("Update item with invalid data changes nothing (rollback)", () -> {
            LibraryService s = fresh();
            assertRejected(IllegalArgumentException.class,
                    () -> s.updateItem("B001", "New Title", "New Author", "2020", "not-an-isbn"));
            assertEquals("Clean Code", s.getItem("B001").getTitle());
            assertEquals("Robert C. Martin", s.getItem("B001").getCreator());
            assertEquals(2008, s.getItem("B001").getYear());
        });

        test("Delete item works when available", () -> {
            LibraryService s = fresh();
            s.removeItem("M001");
            assertTrue(s.findItem("M001").isEmpty(), "item should be gone");
        });

        test("Register and search members", () -> {
            LibraryService s = fresh();
            assertEquals(2, s.searchMembers("").size());
            assertEquals(1, s.searchMembers("maria").size());
            assertEquals(1, s.searchMembers("ana@").size());
        });

        // ---------------- borrowing / returning ----------------
        test("Borrow sets due date: book/student +14, book/faculty +21 (item + member rules)", () -> {
            LibraryService s = fresh();
            s.addItem(ItemType.BOOK, "Second Book", "Someone", "2001", "1234567890");
            Loan a = s.borrowItem("B001", "S001", today);
            Loan b = s.borrowItem("B002", "F001", today);
            assertEquals(today.plusDays(14), a.getDueDate());
            assertEquals(today.plusDays(21), b.getDueDate());
        });

        test("Cannot borrow an item that is already on loan", () -> {
            LibraryService s = fresh();
            s.borrowItem("B001", "S001", today);
            assertRejected(LibraryException.class, () -> s.borrowItem("B001", "F001", today));
        });

        test("Student limit of 3 items is enforced; faculty may borrow more", () -> {
            LibraryService s = fresh();
            s.addItem(ItemType.BOOK, "Book Two", "A", "2001", "1234567890");
            s.addItem(ItemType.BOOK, "Book Three", "A", "2001", "1234567890");
            s.addItem(ItemType.BOOK, "Book Four", "A", "2001", "1234567890");
            s.borrowItem("B001", "S001", today);
            s.borrowItem("B002", "S001", today);
            s.borrowItem("B003", "S001", today);
            assertRejected(LibraryException.class, () -> s.borrowItem("B004", "S001", today));
            s.borrowItem("B004", "F001", today); // faculty limit is 10, so this works
        });

        test("Cannot borrow when the member already has an overdue item", () -> {
            LibraryService s = fresh();
            s.borrowItem("B001", "S001", today.minusDays(30));
            assertRejected(LibraryException.class, () -> s.borrowItem("M001", "S001", today));
        });

        test("Return on time: no fee, item becomes available again", () -> {
            LibraryService s = fresh();
            Loan loan = s.borrowItem("B001", "S001", today.minusDays(5));
            s.returnItem(loan.getId(), today);
            assertMoney(0.0, loan.getFeeCharged());
            assertTrue(s.getItem("B001").isAvailable(), "item should be available");
        });

        test("Late fee - Book, Student, 10 days late = 10 x 5 = 50", () -> {
            LibraryService s = fresh();
            Loan loan = s.borrowItem("B001", "S001", today.minusDays(24)); // due 10 days ago
            s.returnItem(loan.getId(), today);
            assertEquals(10L, loan.getDaysLate(today));
            assertMoney(50.0, loan.getFeeCharged());
        });

        test("Late fee - Book, Faculty, 10 days late = 25 (50% discount)", () -> {
            LibraryService s = fresh();
            Loan loan = s.borrowItem("B001", "F001", today.minusDays(31)); // due 10 days ago (21-day loan)
            s.returnItem(loan.getId(), today);
            assertMoney(25.0, loan.getFeeCharged());
        });

        test("Late fee - Magazine, Student, 4 days late = 4 x 3 = 12", () -> {
            LibraryService s = fresh();
            Loan loan = s.borrowItem("M001", "S001", today.minusDays(11)); // due 4 days ago
            s.returnItem(loan.getId(), today);
            assertMoney(12.0, loan.getFeeCharged());
        });

        test("Late fee - DVD, Student, 33 days late is capped at 500 (not 660)", () -> {
            LibraryService s = fresh();
            Loan loan = s.borrowItem("D001", "S001", today.minusDays(36)); // due 33 days ago
            s.returnItem(loan.getId(), today);
            assertMoney(500.0, loan.getFeeCharged());
        });

        test("Total fees collected is tracked", () -> {
            LibraryService s = fresh();
            Loan loan = s.borrowItem("B001", "S001", today.minusDays(24));
            s.returnItem(loan.getId(), today);
            assertMoney(50.0, s.getTotalFeesCollected());
        });

        test("Overdue report lists only late, unreturned loans", () -> {
            LibraryService s = fresh();
            s.borrowItem("B001", "S001", today.minusDays(30)); // overdue
            s.borrowItem("M001", "F001", today.minusDays(1));  // fine
            assertEquals(1, s.getOverdueLoans(today).size());
            assertEquals(2, s.getOverdueLoans(today.plusDays(30)).size());
        });

        test("Cannot delete an item on loan, but can after it is returned", () -> {
            LibraryService s = fresh();
            Loan loan = s.borrowItem("B001", "S001", today);
            assertRejected(LibraryException.class, () -> s.removeItem("B001"));
            s.returnItem(loan.getId(), today);
            s.removeItem("B001");
        });

        test("Cannot delete a member who still has borrowed items", () -> {
            LibraryService s = fresh();
            Loan loan = s.borrowItem("B001", "S001", today);
            assertRejected(LibraryException.class, () -> s.removeMember("S001"));
            s.returnItem(loan.getId(), today);
            s.removeMember("S001");
        });

        // ---------------- input validation ----------------
        test("Validation: blank title / author rejected", () -> {
            LibraryService s = fresh();
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.BOOK, "  ", "Author", "2000", "1234567890"));
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.BOOK, "Title", "", "2000", "1234567890"));
        });

        test("Validation: year must be a number in a sensible range", () -> {
            LibraryService s = fresh();
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.BOOK, "T", "A", "abc", "1234567890"));
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.BOOK, "T", "A", "999", "1234567890"));
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.BOOK, "T", "A", "3000", "1234567890"));
        });

        test("Validation: ISBN, issue number and duration formats", () -> {
            LibraryService s = fresh();
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.BOOK, "T", "A", "2000", "12345"));
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.MAGAZINE, "T", "A", "2000", "0"));
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.DVD, "T", "A", "2000", "-5"));
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.DVD, "T", "A", "2000", "abc"));
        });

        test("Validation: a rejected item does not use up an ID or get stored", () -> {
            LibraryService s = fresh();
            assertRejected(IllegalArgumentException.class,
                    () -> s.addItem(ItemType.BOOK, "T", "A", "2000", "bad"));
            assertEquals("B002", s.addItem(ItemType.BOOK, "T", "A", "2000", "1234567890").getId());
        });

        test("Validation: member name and email", () -> {
            LibraryService s = fresh();
            assertRejected(IllegalArgumentException.class,
                    () -> s.addMember(MemberType.STUDENT, "", "x@example.com"));
            assertRejected(IllegalArgumentException.class,
                    () -> s.addMember(MemberType.STUDENT, "John123", "x@example.com"));
            assertRejected(IllegalArgumentException.class,
                    () -> s.addMember(MemberType.STUDENT, "John Smith", "not-an-email"));
        });

        test("Validation: duplicate member email rejected (case-insensitive)", () -> {
            LibraryService s = fresh();
            assertRejected(LibraryException.class,
                    () -> s.addMember(MemberType.STUDENT, "Other Person", "MARIA@example.com"));
        });

        test("Validation: borrow date in future, return date before borrow / in future rejected", () -> {
            LibraryService s = fresh();
            assertRejected(LibraryException.class, () -> s.borrowItem("B001", "S001", today.plusDays(1)));
            Loan loan = s.borrowItem("B001", "S001", today.minusDays(3));
            assertRejected(LibraryException.class, () -> s.returnItem(loan.getId(), today.minusDays(5)));
            assertRejected(LibraryException.class, () -> s.returnItem(loan.getId(), today.plusDays(1)));
        });

        test("Error handling: unknown item / member / loan IDs", () -> {
            LibraryService s = fresh();
            assertRejected(LibraryException.class, () -> s.borrowItem("X999", "S001", today));
            assertRejected(LibraryException.class, () -> s.borrowItem("B001", "X999", today));
            assertRejected(LibraryException.class, () -> s.returnItem(424242, today));
        });

        // ---------------- OOP pillars ----------------
        test("Encapsulation: every instance field in the model classes is private", () -> {
            Class<?>[] classes = {LibraryItem.class, Book.class, Magazine.class, DVD.class,
                    Member.class, Student.class, Faculty.class, Loan.class};
            for (Class<?> c : classes) {
                for (Field f : c.getDeclaredFields()) {
                    if (!Modifier.isStatic(f.getModifiers())) {
                        assertTrue(Modifier.isPrivate(f.getModifiers()),
                                c.getSimpleName() + "." + f.getName() + " is not private");
                    }
                }
            }
        });

        test("Inheritance: Book/Magazine/DVD extend LibraryItem; Student/Faculty extend Member", () -> {
            assertTrue(LibraryItem.class.isAssignableFrom(Book.class), "Book");
            assertTrue(LibraryItem.class.isAssignableFrom(Magazine.class), "Magazine");
            assertTrue(LibraryItem.class.isAssignableFrom(DVD.class), "DVD");
            assertTrue(Member.class.isAssignableFrom(Student.class), "Student");
            assertTrue(Member.class.isAssignableFrom(Faculty.class), "Faculty");
        });

        test("Abstraction: LibraryItem and Member are abstract; Borrowable is an interface", () -> {
            assertTrue(Modifier.isAbstract(LibraryItem.class.getModifiers()), "LibraryItem");
            assertTrue(Modifier.isAbstract(Member.class.getModifiers()), "Member");
            assertTrue(Borrowable.class.isInterface(), "Borrowable");
            assertTrue(Borrowable.class.isAssignableFrom(LibraryItem.class), "implements Borrowable");
        });

        test("Polymorphism: same method call, different behaviour per subclass", () -> {
            List<LibraryItem> items = List.of(
                    new Book("B1", "T", "A", 2000),
                    new Magazine("M1", "T", "A", 2000),
                    new DVD("D1", "T", "A", 2000));
            // same call on the parent type: getLoanPeriodDays() / calculateLateFee()
            assertEquals(14, items.get(0).getLoanPeriodDays());
            assertEquals(7, items.get(1).getLoanPeriodDays());
            assertEquals(3, items.get(2).getLoanPeriodDays());
            assertMoney(50.0, items.get(0).calculateLateFee(10));
            assertMoney(30.0, items.get(1).calculateLateFee(10));
            assertMoney(200.0, items.get(2).calculateLateFee(10));
            assertMoney(500.0, items.get(2).calculateLateFee(100)); // DVD override: cap

            List<Member> members = List.of(new Student("S1", "A B", "a@b.co"), new Faculty("F1", "C D", "c@d.co"));
            assertEquals(3, members.get(0).getMaxLoans());
            assertEquals(10, members.get(1).getMaxLoans());
            assertMoney(100.0, members.get(0).applyDiscount(100));
            assertMoney(50.0, members.get(1).applyDiscount(100));
        });

        System.out.println();
        System.out.println("Result: " + passed + " passed, " + failed + " failed (" + (passed + failed) + " tests)");
        if (failed > 0) {
            System.exit(1);
        }
    }
}
