package at.fhtw.paperless.rest.exception;
public class CategoryInUseException extends RuntimeException {
    public CategoryInUseException(String message) { super(message); }
    public CategoryInUseException(String message, Throwable cause) { super(message, cause); }
}
