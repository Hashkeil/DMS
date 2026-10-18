package at.fhtw.paperless.rest.exception;
public class DocumentPublishException extends RuntimeException {
    public DocumentPublishException(String message) { super(message); }
    public DocumentPublishException(String message, Throwable cause) { super(message, cause); }
}
