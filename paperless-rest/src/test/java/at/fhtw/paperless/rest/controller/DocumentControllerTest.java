package at.fhtw.paperless.rest.controller;

import at.fhtw.paperless.rest.dto.*;
import at.fhtw.paperless.rest.exception.*;
import at.fhtw.paperless.rest.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentControllerTest {
    private final DocumentService service = mock(DocumentService.class);
    private MockMvc mvc;
    private final DocumentResponse response = new DocumentResponse(1L, "Invoice", "invoice.pdf", null, null, null, null, null);
    private final String validBody = """
            {"title":"Invoice","filename":"invoice.pdf"}
            """;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DocumentController(service))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
    }

    @Test
    void createReturns201AndLocation() throws Exception {
        when(service.create(any())).thenReturn(response);
        mvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content(validBody))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/documents/1"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAndListReturn200() throws Exception {
        when(service.getById(1L)).thenReturn(response);
        when(service.getAll()).thenReturn(List.of(response));
        mvc.perform(get("/api/documents/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
        mvc.perform(get("/api/documents")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void updateReturns200() throws Exception {
        when(service.update(eq(1L), any())).thenReturn(response);
        mvc.perform(put("/api/documents/1").contentType(MediaType.APPLICATION_JSON).content(validBody))
                .andExpect(status().isOk()).andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/documents/1")).andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(service).delete(1L);
    }

    @Test
    void missingResourceReturnsClean404() throws Exception {
        when(service.getById(99L)).thenThrow(new DocumentNotFoundException(99L));
        mvc.perform(get("/api/documents/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.path").value("/api/documents/99"))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.trace").doesNotExist());
    }

    @Test
    void invalidCreateAndUpdateReturn400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.status").value(400));
        mvc.perform(put("/api/documents/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }

    @Test
    void malformedBodyAndIdentifierReturn400() throws Exception {
        mvc.perform(post("/api/documents").contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/documents/abc")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
