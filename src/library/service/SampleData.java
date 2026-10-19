package library.service;

import java.time.LocalDate;

import library.model.ItemType;
import library.model.MemberType;

/** Fills a new LibraryService with demo data so the GUI is not empty on first launch. */
public final class SampleData {

    private SampleData() {
    }

    public static void load(LibraryService s) {
        s.addItem(ItemType.BOOK, "Noli Me Tangere", "Jose Rizal", "1887", "9789715670001");
        s.addItem(ItemType.BOOK, "El Filibusterismo", "Jose Rizal", "1891", "9789715670018");
        s.addItem(ItemType.BOOK, "Clean Code", "Robert C. Martin", "2008", "9780132350884");
        s.addItem(ItemType.BOOK, "Head First Java", "Kathy Sierra", "2005", "9780596009205");
        s.addItem(ItemType.BOOK, "Introduction to Algorithms", "Thomas H. Cormen", "2009", "9780262033848");
        s.addItem(ItemType.MAGAZINE, "National Geographic", "National Geographic Society", "2026", "10");
        s.addItem(ItemType.MAGAZINE, "TIME", "TIME USA", "2026", "12");
        s.addItem(ItemType.DVD, "The Social Network", "David Fincher", "2010", "120");
        s.addItem(ItemType.DVD, "Planet Earth", "BBC Earth", "2006", "550");

        try {
            s.addMember(MemberType.STUDENT, "Maria Santos", "maria.santos@example.com");
            s.addMember(MemberType.STUDENT, "Juan Dela Cruz", "juan.delacruz@example.com");
            s.addMember(MemberType.FACULTY, "Dr. Ana Reyes", "ana.reyes@example.com");

            LocalDate today = LocalDate.now();
            // Juan borrowed Clean Code 30 days ago: due after 14 days, so it is overdue.
            s.borrowItem("B003", "S002", today.minusDays(30));
            // Maria borrowed a magazine 2 days ago: still on time.
            s.borrowItem("M001", "S001", today.minusDays(2));
            // Dr. Reyes borrowed a DVD yesterday.
            s.borrowItem("D001", "F001", today.minusDays(1));
        } catch (LibraryException ex) {
            throw new IllegalStateException("Sample data is inconsistent: " + ex.getMessage(), ex);
        }
    }
}
