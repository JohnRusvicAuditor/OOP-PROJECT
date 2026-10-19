package library.model;

/** The kinds of library items the system can hold. */
public enum ItemType {
    BOOK("Book", "B", "ISBN"),
    MAGAZINE("Magazine", "M", "Issue No."),
    DVD("DVD", "D", "Duration (min)");

    private final String label;
    private final String idPrefix;
    private final String extraLabel;

    ItemType(String label, String idPrefix, String extraLabel) {
        this.label = label;
        this.idPrefix = idPrefix;
        this.extraLabel = extraLabel;
    }

    public String getLabel() {
        return label;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    /** Name of the extra, type-specific field (ISBN, issue number, duration). */
    public String getExtraLabel() {
        return extraLabel;
    }

    @Override
    public String toString() {
        return label;
    }
}
