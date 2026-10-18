package at.fhtw.paperless.rest.storage;
import org.springframework.web.multipart.MultipartFile;
public interface DocumentStorage {
    String store(MultipartFile file);
    void delete(String key);
}
