package at.fhtw.paperless.rest.controller;

import at.fhtw.paperless.rest.dto.DocumentResponse;
import at.fhtw.paperless.rest.service.DocumentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Full-text search over the OCR text stored in Elasticsearch. */
@RestController
@RequestMapping("/api/documents")
public class SearchController {
    private final RestClient elastic;
    private final ObjectMapper json;
    private final DocumentService documents;

    public SearchController(@Value("${paperless.elasticsearch.url}") String url, ObjectMapper json,
                            DocumentService documents) {
        this.elastic = RestClient.create(url);
        this.json = json;
        this.documents = documents;
    }

    @GetMapping("/search")
    public List<DocumentResponse> search(@RequestParam String q) {
        String query = json.writeValueAsString(Map.of("query", Map.of("match", Map.of("content", q))));
        String result = elastic.post().uri("/documents/_search?ignore_unavailable=true")
                .contentType(MediaType.APPLICATION_JSON).body(query).retrieve().body(String.class);
        List<Long> ids = new ArrayList<>();
        json.readTree(result).path("hits").path("hits").forEach(hit -> ids.add(Long.valueOf(hit.path("_id").asString())));
        return documents.getByIds(ids);
    }
}
