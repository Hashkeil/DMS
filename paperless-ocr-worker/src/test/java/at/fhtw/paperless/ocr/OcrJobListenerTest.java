package at.fhtw.paperless.ocr;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.core.*;
import tools.jackson.databind.json.JsonMapper;
import static org.junit.jupiter.api.Assertions.*;

class OcrJobListenerTest {
    final OcrJobListener listener = new OcrJobListener(JsonMapper.builder().build());
    Message message(String json) { return new Message(json.getBytes(), new MessageProperties()); }
    @Test void acceptsValidJobWithoutOcr() {
        assertDoesNotThrow(() -> listener.receive(message("""
            {"documentId":1,"filename":"test.pdf","storageKey":"12345678-1234-1234-1234-123456789abc.pdf"}
            """)));
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
