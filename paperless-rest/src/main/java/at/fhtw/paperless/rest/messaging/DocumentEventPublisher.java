package at.fhtw.paperless.rest.messaging;
public interface DocumentEventPublisher {
    void publish(DocumentUploadedEvent event);
}
