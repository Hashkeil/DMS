package at.fhtw.paperless.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class  OcrJobListener {
    private static final Logger log = LoggerFactory.getLogger(OcrJobListener.class);
    private final ObjectMapper json;
    public OcrJobListener(ObjectMapper json) { this.json = json; }

    // Both applications declare the same durable topology, so either can start first.
    @Bean
    Queue ocrQueue() {
        return QueueBuilder.durable("paperless.ocr")
                .deadLetterExchange("").deadLetterRoutingKey("paperless.ocr.failed").build();
    }
    @Bean
    Queue failedQueue() { return QueueBuilder.durable("paperless.ocr.failed").build(); }

    @RabbitListener(queues = "paperless.ocr")
    public void receive(Message message) {
        try {
            var event = json.readValue(message.getBody(), OcrJob.class);
            if (event.documentId() == null || event.documentId() <= 0
                    || event.filename() == null || event.filename().isBlank() || event.filename().length() > 255
                    || event.storageKey() == null || !event.storageKey().matches("[0-9a-f-]{36}\\.pdf")) {
                throw new IllegalArgumentException("Invalid OCR job fields");
            }
            log.info("OCR job received: documentId={}, filename={}, storageKey={} (OCR deferred to Sprint 4)",
                    event.documentId(), event.filename().replaceAll("[\\r\\n]", "_"), event.storageKey());
            // Successful return acknowledges the message. No OCR or database access in Sprint 3.
        } catch (Exception failure) {
            log.warn("Rejected invalid OCR message; forwarding to failed queue: messageId={}",
                    message.getMessageProperties().getMessageId(), failure);
            throw new AmqpRejectAndDontRequeueException("Invalid OCR job", failure);
        }
    }

    public record OcrJob(Long documentId, String filename, String storageKey) {}
}
