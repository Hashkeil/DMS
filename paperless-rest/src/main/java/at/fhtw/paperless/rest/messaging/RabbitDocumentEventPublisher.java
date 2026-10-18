package at.fhtw.paperless.rest.messaging;

import at.fhtw.paperless.rest.exception.DocumentPublishException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class RabbitDocumentEventPublisher implements DocumentEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(RabbitDocumentEventPublisher.class);
    private final RabbitTemplate rabbit;
    private final ObjectMapper json;

    public RabbitDocumentEventPublisher(RabbitTemplate rabbit, ObjectMapper json) {
        this.rabbit = rabbit;
        this.json = json;
    }

    @Override
    public void publish(DocumentUploadedEvent event) {
        CorrelationData correlation = new CorrelationData(UUID.randomUUID().toString());
        try {
            var message = MessageBuilder.withBody(json.writeValueAsBytes(event))
                    .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                    .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                    .setMessageId(correlation.getId()).build();
            rabbit.send("", RabbitConfiguration.QUEUE, message, correlation);
            var confirm = correlation.getFuture().get(5, TimeUnit.SECONDS);
            if (!confirm.ack() || correlation.getReturned() != null) {
                throw new DocumentPublishException("OCR message was not accepted by the queue");
            }
            log.info("OCR message accepted: documentId={}, messageId={}", event.documentId(), correlation.getId());
        } catch (InterruptedException failure) {
            Thread.currentThread().interrupt();
            throw new DocumentPublishException("Interrupted while publishing OCR message", failure);
        } catch (DocumentPublishException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new DocumentPublishException("OCR queue is unavailable", failure);
        }
    }
}
