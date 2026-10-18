package at.fhtw.paperless.rest.exception;

public class LabelNotFoundException extends RuntimeException {
    public LabelNotFoundException(long id) {
        super("Label with id " + id + " not found");
    }
}
