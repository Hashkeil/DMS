 package at.fhtw.paperless.rest.storage;

import at.fhtw.paperless.rest.exception.DocumentStorageException;
import at.fhtw.paperless.rest.exception.InvalidDocumentException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/** Stores PDFs in RustFS (S3-compatible). */
@Component
public class RustFsDocumentStorage implements DocumentStorage {
    private static final long MAX_BYTES = 10 * 1024 * 1024;
    private final S3Client s3;
    private final String bucket;

    public RustFsDocumentStorage(@Value("${paperless.rustfs.url}") String url,
                                 @Value("${paperless.rustfs.access-key}") String accessKey,
                                 @Value("${paperless.rustfs.secret-key}") String secretKey,
                                 @Value("${paperless.rustfs.bucket}") String bucket) {
        this.s3 = S3Client.builder().endpointOverride(URI.create(url)).region(Region.US_EAST_1).forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        this.bucket = bucket;
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty() || file.getSize() > MAX_BYTES) {
            throw new InvalidDocumentException("Provide a non-empty PDF no larger than 10 MiB");
        }
        String key = UUID.randomUUID() + ".pdf";
        try (var input = file.getInputStream()) {
            if (!new String(Arrays.copyOf(file.getBytes(), 5), StandardCharsets.US_ASCII).equals("%PDF-")) {
                throw new InvalidDocumentException("File must contain a PDF header");
            }
            try { s3.headBucket(b -> b.bucket(bucket)); }
            catch (NoSuchBucketException missing) { s3.createBucket(b -> b.bucket(bucket)); }
            s3.putObject(b -> b.bucket(bucket).key(key).contentType("application/pdf"),
                    RequestBody.fromInputStream(input, file.getSize()));
            return key;
        } catch (InvalidDocumentException invalid) {
            throw invalid;
        } catch (Exception failure) {
            throw new DocumentStorageException("Could not store PDF", failure);
        }
    }

    @Override
    public void delete(String key) {
        try { s3.deleteObject(b -> b.bucket(bucket).key(key)); }
        catch (Exception failure) { throw new DocumentStorageException("Could not remove PDF", failure); }
    }
}
