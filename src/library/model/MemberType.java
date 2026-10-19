package library.model;

/** The kinds of library members. */
public enum MemberType {
    STUDENT("Student", "S"),
    FACULTY("Faculty", "F");

    private final String label;
    private final String idPrefix;

    MemberType(String label, String idPrefix) {
        this.label = label;
        this.idPrefix = idPrefix;
    }

    public String getLabel() {
        return label;
    }

    public String getIdPrefix() {
        return idPrefix;
    }

    @Override
    public String toString() {
        return label;
    }
}
