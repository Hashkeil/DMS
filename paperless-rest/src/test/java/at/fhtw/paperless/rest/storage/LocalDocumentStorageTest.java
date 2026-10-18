package at.fhtw.paperless.rest.storage;
import at.fhtw.paperless.rest.exception.InvalidDocumentException;
import at.fhtw.paperless.rest.exception.DocumentStorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class LocalDocumentStorageTest {
    @TempDir Path directory;
    @Test void savesUsingGeneratedNameAndDeletes() throws Exception {
        var storage = new LocalDocumentStorage(directory.toString());
        byte[] bytes = "%PDF-1.4\nexample".getBytes();
        var file = new MockMultipartFile("file", "../../escape.pdf", "application/pdf", bytes);
        String key = storage.store(file);
        assertTrue(key.matches("[0-9a-f-]{36}\\.pdf"));
        assertArrayEquals(bytes, Files.readAllBytes(directory.resolve(key)));
        storage.delete(key);
        assertFalse(Files.exists(directory.resolve(key)));
    }
    @Test void rejectsEmptyAndFakePdf() {
        var storage = new LocalDocumentStorage(directory.toString());
        assertThrows(InvalidDocumentException.class, () -> storage.store(
            new MockMultipartFile("file", new byte[0])));
        assertThrows(InvalidDocumentException.class, () -> storage.store(
            new MockMultipartFile("file", "fake.pdf", "application/pdf", "not pdf".getBytes())));
    }
    @Test void rejectsOversizedPdf() {
        var storage = new LocalDocumentStorage(directory.toString());
        assertThrows(InvalidDocumentException.class, () -> storage.store(
            new MockMultipartFile("file", new byte[(int) LocalDocumentStorage.MAX_BYTES + 1])));
    }
    @Test void rejectsUnsafeDeleteKey() {
        assertThrows(DocumentStorageException.class,
            () -> new LocalDocumentStorage(directory.toString()).delete("../outside.pdf"));
    }
}
