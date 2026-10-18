package at.fhtw.paperless.rest.messaging;
import at.fhtw.paperless.rest.exception.DocumentPublishException;
import org.junit.jupiter.api.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

class RabbitDocumentEventPublisherTest {
    RabbitTemplate rabbit = mock(RabbitTemplate.class);
    RabbitDocumentEventPublisher publisher = new RabbitDocumentEventPublisher(rabbit, JsonMapper.builder().build());
    DocumentUploadedEvent event = new DocumentUploadedEvent(1L, "test.pdf", "key.pdf");
    @Test void publishesPersistentJsonAndWaitsForAck() {
        doAnswer(call -> {
            Message message = call.getArgument(2);
            assertEquals(MessageDeliveryMode.PERSISTENT, message.getMessageProperties().getDeliveryMode());
            assertEquals("application/json", message.getMessageProperties().getContentType());
            assertTrue(new String(message.getBody()).contains("\"documentId\":1"));
            CorrelationData correlation = call.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbit).send(eq(""), eq("paperless.ocr"), any(Message.class), any(CorrelationData.class));
        assertDoesNotThrow(() -> publisher.publish(event));
    }
    @Test void rejectsBrokerNack() {
        doAnswer(call -> {
            CorrelationData correlation = call.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(false, "failed"));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        assertThrows(DocumentPublishException.class, () -> publisher.publish(event));
    }
    @Test void rejectsUnroutableMessageEvenWhenAcked() {
        doAnswer(call -> {
            CorrelationData correlation = call.getArgument(3);
            correlation.setReturned(new ReturnedMessage(call.getArgument(2), 312, "NO_ROUTE", "", "paperless.ocr"));
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        assertThrows(DocumentPublishException.class, () -> publisher.publish(event));
    }
    @Test void wrapsConnectionFailure() {
        doThrow(new org.springframework.amqp.AmqpConnectException(new java.net.ConnectException()))
            .when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
        assertThrows(DocumentPublishException.class, () -> publisher.publish(event));
    }
}
