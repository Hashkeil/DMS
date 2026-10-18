package at.fhtw.paperless.rest.messaging;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfiguration {
    public static final String QUEUE = "paperless.ocr";
    @Bean
    Queue ocrQueue() {
        return QueueBuilder.durable(QUEUE)
                .deadLetterExchange("").deadLetterRoutingKey(QUEUE + ".failed").build();
    }
    @Bean
    Queue failedQueue() { return QueueBuilder.durable(QUEUE + ".failed").build(); }
}
