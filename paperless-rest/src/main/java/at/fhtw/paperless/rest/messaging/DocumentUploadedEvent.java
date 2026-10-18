package at.fhtw.paperless.rest.messaging;
public record DocumentUploadedEvent(Long documentId, String filename, String storageKey) {}
