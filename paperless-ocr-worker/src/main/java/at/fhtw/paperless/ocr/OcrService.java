package at.fhtw.paperless.ocr;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Fetches the PDF from RustFS, renders it with Ghostscript and runs Tesseract on it. */
@Service
public class OcrService {
    private final S3Client s3;
    private final String bucket;

    public OcrService(@Value("${paperless.rustfs.url}") String url,
                      @Value("${paperless.rustfs.access-key}") String accessKey,
                      @Value("${paperless.rustfs.secret-key}") String secretKey,
                      @Value("${paperless.rustfs.bucket}") String bucket) {
        this.s3 = S3Client.builder().endpointOverride(URI.create(url)).region(Region.US_EAST_1).forcePathStyle(true)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
        this.bucket = bucket;
    }

    public String extractText(String storageKey) throws IOException, InterruptedException {
        Path pdf = Files.createTempFile("ocr-", ".pdf");
        Path tiff = Files.createTempFile("ocr-", ".tif");
        try {
            Files.write(pdf, s3.getObjectAsBytes(b -> b.bucket(bucket).key(storageKey)).asByteArray());
            // Ghostscript: all pages into one multi-page TIFF; Tesseract: TIFF -> text on stdout.
            run("gs", "-q", "-dSAFER", "-dBATCH", "-dNOPAUSE", "-sDEVICE=tiffgray", "-r300",
                    "-sOutputFile=" + tiff, pdf.toString());
            return run("tesseract", tiff.toString(), "stdout").trim();
        } finally {
            Files.deleteIfExists(pdf);
            Files.deleteIfExists(tiff);
        }
    }

    private static String run(String... command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).redirectError(ProcessBuilder.Redirect.DISCARD).start();
        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        if (process.waitFor() != 0) throw new IOException(command[0] + " failed");
        return output;
    }
}
