package at.fhtw.paperless.ocr;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OcrJobListenerTest {
    final OcrService ocr = mock(OcrService.class);
    final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    final OcrJobListener listener = new OcrJobListener(JsonMapper.builder().build(), ocr, rabbit);
    Message message(String json) { return new Message(json.getBytes(), new MessageProperties()); }
    @Test void runsOcrAndForwardsTextToIndexQueue() throws Exception {
        when(ocr.extractText(any())).thenReturn("Hello World");
        listener.receive(message("""
            {"documentId":1,"filename":"test.pdf","storageKey":"12345678-1234-1234-1234-123456789abc.pdf"}
            """));
        verify(rabbit).send(eq(""), eq(OcrJobListener.INDEX_QUEUE), any(Message.class));
    }
    @Test void rejectsMalformedJsonWithoutRequeue() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receive(message("{")));
    }
    @Test void rejectsMissingFieldsWithoutRequeue() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receive(message("{}")));
    }
    @Test void rejectsUnsafeStorageKey() {
        assertThrows(AmqpRejectAndDontRequeueException.class, () -> listener.receive(message("""
            {"documentId":1,"filename":"test.pdf","storageKey":"../outside.pdf"}
            """)));
    }
}
