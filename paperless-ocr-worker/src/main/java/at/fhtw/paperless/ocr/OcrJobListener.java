package at.fhtw.paperless.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class  OcrJobListener {
    public static final String INDEX_QUEUE = "paperless.index";
    private static final Logger log = LoggerFactory.getLogger(OcrJobListener.class);
    private final ObjectMapper json;
    private final OcrService ocr;
    private final RabbitTemplate rabbit;

    public OcrJobListener(ObjectMapper json, OcrService ocr, RabbitTemplate rabbit) {
        this.json = json;
        this.ocr = ocr;
        this.rabbit = rabbit;
    }

    // Both applications declare the same durable topology, so either can start first.
    @Bean
    Queue ocrQueue() {
        return QueueBuilder.durable("paperless.ocr")
                .deadLetterExchange("").deadLetterRoutingKey("paperless.ocr.failed").build();
    }
    @Bean
    Queue failedQueue() { return QueueBuilder.durable("paperless.ocr.failed").build(); }
    @Bean
    Queue indexQueue() { return QueueBuilder.durable(INDEX_QUEUE).build(); }

    @RabbitListener(queues = "paperless.ocr")
    public void receive(Message message) {
        try {
            var event = json.readValue(message.getBody(), OcrJob.class);
            if (event.documentId() == null || event.documentId() <= 0
                    || event.filename() == null || event.filename().isBlank() || event.filename().length() > 255
                    || event.storageKey() == null || !event.storageKey().matches("[0-9a-f-]{36}\\.pdf")) {
                throw new IllegalArgumentException("Invalid OCR job fields");
            }
            String text = ocr.extractText(event.storageKey());
            log.info("OCR done: documentId={}, characters={}", event.documentId(), text.length());
            // Hand the OCR result to the indexing worker.
            rabbit.send("", INDEX_QUEUE, new Message(json.writeValueAsBytes(new OcrResult(event.documentId(), text))));
        } catch (Exception failure) {
            log.warn("OCR job failed; forwarding to failed queue: messageId={}",
                    message.getMessageProperties().getMessageId(), failure);
            throw new AmqpRejectAndDontRequeueException("OCR job failed", failure);
        }
    }

    public record OcrJob(Long documentId, String filename, String storageKey) {}
    public record OcrResult(Long documentId, String content) {}
}
