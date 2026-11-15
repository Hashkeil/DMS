package at.fhtw.paperless.ocr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/** Indexing worker: stores the OCR text in Elasticsearch (index "documents", id = documentId). */
@Component
public class IndexingListener {
    private static final Logger log = LoggerFactory.getLogger(IndexingListener.class);
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json;
    private final String elasticUrl;

    public IndexingListener(ObjectMapper json, @Value("${paperless.elasticsearch.url}") String elasticUrl) {
        this.json = json;
        this.elasticUrl = elasticUrl;
    }

    @RabbitListener(queues = OcrJobListener.INDEX_QUEUE)
    public void receive(Message message) throws Exception {
        var result = json.readValue(message.getBody(), OcrJobListener.OcrResult.class);
        var request = HttpRequest.newBuilder(URI.create(elasticUrl + "/documents/_doc/" + result.documentId()))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofByteArray(message.getBody())).build();
        var response = http.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() >= 300) throw new IllegalStateException("Elasticsearch error: " + response.body());
        log.info("Indexed document: documentId={}", result.documentId());
    }
}
