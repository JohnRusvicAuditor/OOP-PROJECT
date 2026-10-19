package library.service;

/** Thrown when a request breaks a library business rule (not found, limit reached, ...). */
public class LibraryException extends Exception {

    public LibraryException(String message) {
        super(message);
    }
}
