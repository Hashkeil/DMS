package at.fhtw.paperless.rest.storage;

import at.fhtw.paperless.rest.exception.DocumentStorageException;
import at.fhtw.paperless.rest.exception.InvalidDocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.UUID;

@Component
public class LocalDocumentStorage implements DocumentStorage {
    public static final long MAX_BYTES = 10 * 1024 * 1024;
    private final Path directory;

    public LocalDocumentStorage(@Value("${paperless.storage.directory:./uploads}") String directory) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
            throw new InvalidDocumentException("Provide a non-empty PDF no larger than 10 MiB");
        }
        String key = UUID.randomUUID() + ".pdf";
        Path target = directory.resolve(key);
        try (var input = file.getInputStream()) {
            byte[] signature = input.readNBytes(5);
            if (!"%PDF-".equals(new String(signature, StandardCharsets.US_ASCII))) {
                throw new InvalidDocumentException("File must contain a PDF header");
            }
            Files.createDirectories(directory);
            // Never use a client-supplied filename as a filesystem path.
            try (var output = Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)) {
                output.write(signature);
                byte[] buffer = new byte[8192];
                long total = signature.length;
                int count;
                while ((count = input.read(buffer)) != -1) {
                    total += count;
                    if (total > MAX_BYTES) throw new InvalidDocumentException("PDF exceeds 10 MiB");
                    output.write(buffer, 0, count);
                }
            }
            return key;
        } catch (IOException | RuntimeException failure) {
            try { Files.deleteIfExists(target); } catch (IOException cleanup) { failure.addSuppressed(cleanup); }
            if (failure instanceof InvalidDocumentException invalid) throw invalid;
            throw new DocumentStorageException("Could not store PDF", failure);
        }
    }

    @Override
    public void delete(String key) {
        if (key == null || !key.matches("[0-9a-f-]{36}\\.pdf")) {
            throw new DocumentStorageException("Invalid storage key");
        }
        try { Files.deleteIfExists(directory.resolve(key)); }
        catch (IOException failure) { throw new DocumentStorageException("Could not remove PDF", failure); }
    }
}
