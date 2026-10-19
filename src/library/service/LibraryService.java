package library.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import library.model.Book;
import library.model.DVD;
import library.model.Faculty;
import library.model.ItemType;
import library.model.LibraryItem;
import library.model.Loan;
import library.model.Magazine;
import library.model.Member;
import library.model.MemberType;
import library.model.Student;
import library.util.Validator;

/**
 * The "brain" of the application. The GUI never changes data directly; it
 * asks this class, which enforces every rule and validates every input.
 *
 * Works only with the abstract types LibraryItem and Member, so it does not
 * care which concrete subclass it is handling (POLYMORPHISM).
 */
public class LibraryService {

    private final Map<String, LibraryItem> items = new LinkedHashMap<>();
    private final Map<String, Member> members = new LinkedHashMap<>();
    private final List<Loan> loans = new ArrayList<>();
    private final Map<String, Integer> idCounters = new HashMap<>();
    private int loanCounter = 1000;
    private double totalFeesCollected = 0.0;

    // ==================== ITEMS ====================

    /** Creates and stores a new item; the ID is generated automatically (B001, M001, D001...). */
    public LibraryItem addItem(ItemType type, String title, String creator, String year, String extra) {
        int parsedYear = Validator.parseInt("Year", year, 1000, LocalDate.now().getYear() + 1);
        int next = idCounters.getOrDefault(type.getIdPrefix(), 0) + 1;
        String id = type.getIdPrefix() + String.format("%03d", next);

        LibraryItem item = switch (type) {
            case BOOK -> new Book(id, title, creator, parsedYear);
            case MAGAZINE -> new Magazine(id, title, creator, parsedYear);
            case DVD -> new DVD(id, title, creator, parsedYear);
        };
        item.setExtraValue(extra);

        idCounters.put(type.getIdPrefix(), next);
        items.put(id, item);
        return item;
    }

    public Optional<LibraryItem> findItem(String id) {
        return Optional.ofNullable(items.get(id));
    }

    public LibraryItem getItem(String id) throws LibraryException {
        return findItem(id).orElseThrow(() -> new LibraryException("Item not found: " + id));
    }

    /** Updates an item. If any value is invalid, nothing is changed. */
    public void updateItem(String id, String title, String creator, String year, String extra)
            throws LibraryException {
        LibraryItem item = getItem(id);
        String oldTitle = item.getTitle();
        String oldCreator = item.getCreator();
        int oldYear = item.getYear();
        String oldExtra = item.getExtraValue();
        try {
            item.setTitle(title);
            item.setCreator(creator);
            item.setYear(Validator.parseInt("Year", year, 1000, LocalDate.now().getYear() + 1));
            item.setExtraValue(extra);
        } catch (IllegalArgumentException ex) {
            item.setTitle(oldTitle);
            item.setCreator(oldCreator);
            item.setYear(oldYear);
            item.setExtraValue(oldExtra);
            throw ex;
        }
    }

    public void removeItem(String id) throws LibraryException {
        LibraryItem item = getItem(id);
        if (!item.isAvailable()) {
            throw new LibraryException("\"" + item.getTitle() + "\" is currently on loan and cannot be deleted.");
        }
        items.remove(id);
    }

    /** Case-insensitive search on ID, title and author/creator. A null type means all types. */
    public List<LibraryItem> searchItems(String query, ItemType type) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<LibraryItem> result = new ArrayList<>();
        for (LibraryItem item : items.values()) {
            boolean typeOk = type == null || item.getType() == type;
            boolean textOk = q.isEmpty()
                    || item.getId().toLowerCase().contains(q)
                    || item.getTitle().toLowerCase().contains(q)
                    || item.getCreator().toLowerCase().contains(q);
            if (typeOk && textOk) {
                result.add(item);
            }
        }
        return result;
    }

    public List<LibraryItem> getAvailableItems() {
        List<LibraryItem> result = new ArrayList<>();
        for (LibraryItem item : items.values()) {
            if (item.isAvailable()) {
                result.add(item);
            }
        }
        return result;
    }

    // ==================== MEMBERS ====================

    public Member addMember(MemberType type, String name, String email) throws LibraryException {
        String cleanEmail = Validator.requireEmail(email);
        for (Member m : members.values()) {
            if (m.getEmail().equalsIgnoreCase(cleanEmail)) {
                throw new LibraryException("A member with the email " + cleanEmail + " already exists.");
            }
        }
        int next = idCounters.getOrDefault(type.getIdPrefix(), 0) + 1;
        String id = type.getIdPrefix() + String.format("%03d", next);

        Member member = switch (type) {
            case STUDENT -> new Student(id, name, cleanEmail);
            case FACULTY -> new Faculty(id, name, cleanEmail);
        };

        idCounters.put(type.getIdPrefix(), next);
        members.put(id, member);
        return member;
    }

    public Optional<Member> findMember(String id) {
        return Optional.ofNullable(members.get(id));
    }

    public Member getMember(String id) throws LibraryException {
        return findMember(id).orElseThrow(() -> new LibraryException("Member not found: " + id));
    }

    public void removeMember(String id) throws LibraryException {
        Member member = getMember(id);
        if (countActiveLoans(member) > 0) {
            throw new LibraryException(member.getName() + " still has borrowed items and cannot be deleted.");
        }
        members.remove(id);
    }

    public List<Member> searchMembers(String query) {
        String q = query == null ? "" : query.trim().toLowerCase();
        List<Member> result = new ArrayList<>();
        for (Member m : members.values()) {
            if (q.isEmpty()
                    || m.getId().toLowerCase().contains(q)
                    || m.getName().toLowerCase().contains(q)
                    || m.getEmail().toLowerCase().contains(q)) {
                result.add(m);
            }
        }
        return result;
    }

    // ==================== LOANS ====================

    public Loan borrowItem(String itemId, String memberId, LocalDate borrowDate) throws LibraryException {
        if (borrowDate == null) {
            throw new IllegalArgumentException("Borrow date is required.");
        }
        if (borrowDate.isAfter(LocalDate.now())) {
            throw new LibraryException("Borrow date cannot be in the future.");
        }
        LibraryItem item = getItem(itemId);
        Member member = getMember(memberId);

        if (!item.isAvailable()) {
            throw new LibraryException("\"" + item.getTitle() + "\" is already borrowed.");
        }
        if (countActiveLoans(member) >= member.getMaxLoans()) {
            throw new LibraryException(member.getName() + " has reached the limit of "
                    + member.getMaxLoans() + " items for a " + member.getType().getLabel() + ".");
        }
        for (Loan loan : getActiveLoansOf(member)) {
            if (loan.isOverdue(borrowDate)) {
                throw new LibraryException(member.getName()
                        + " has an overdue item (" + loan.getItem().getTitle() + ") and must return it first.");
            }
        }

        item.checkOut();
        Loan loan = new Loan(++loanCounter, item, member, borrowDate);
        loans.add(loan);
        return loan;
    }

    /** Returns an item, computing any late fee. */
    public Loan returnItem(int loanId, LocalDate returnDate) throws LibraryException {
        if (returnDate == null) {
            throw new IllegalArgumentException("Return date is required.");
        }
        Loan loan = null;
        for (Loan l : loans) {
            if (l.getId() == loanId && !l.isReturned()) {
                loan = l;
                break;
            }
        }
        if (loan == null) {
            throw new LibraryException("No active loan with ID " + loanId + ".");
        }
        if (returnDate.isBefore(loan.getBorrowDate())) {
            throw new LibraryException("Return date cannot be before the borrow date ("
                    + loan.getBorrowDate() + ").");
        }
        if (returnDate.isAfter(LocalDate.now())) {
            throw new LibraryException("Return date cannot be in the future.");
        }
        loan.markReturned(returnDate);
        loan.getItem().checkIn();
        totalFeesCollected += loan.getFeeCharged();
        return loan;
    }

    public int countActiveLoans(Member member) {
        return getActiveLoansOf(member).size();
    }

    public List<Loan> getActiveLoansOf(Member member) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isReturned() && loan.getMember() == member) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<Loan> getActiveLoans() {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (!loan.isReturned()) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<Loan> getOverdueLoans(LocalDate asOf) {
        List<Loan> result = new ArrayList<>();
        for (Loan loan : loans) {
            if (loan.isOverdue(asOf)) {
                result.add(loan);
            }
        }
        return result;
    }

    public List<Loan> getAllLoans() {
        return Collections.unmodifiableList(loans);
    }

    // ==================== STATISTICS ====================

    public int getTotalItems() {
        return items.size();
    }

    public int getAvailableItemCount() {
        return getAvailableItems().size();
    }

    public int getTotalMembers() {
        return members.size();
    }

    public int getTotalLoansEver() {
        return loans.size();
    }

    public double getTotalFeesCollected() {
        return totalFeesCollected;
    }
}
