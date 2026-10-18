package at.fhtw.paperless.rest.controller;

import at.fhtw.paperless.rest.dto.DocumentResponse;
import at.fhtw.paperless.rest.exception.*;
import at.fhtw.paperless.rest.service.DocumentUploadService;
import org.junit.jupiter.api.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DocumentUploadControllerTest {
    DocumentUploadService service = mock(DocumentUploadService.class);
    MockMvc mvc;
    MockMultipartFile pdf = new MockMultipartFile("file", "demo.pdf", "application/pdf", "%PDF-1.4".getBytes());
    @BeforeEach void setup() {
        mvc = MockMvcBuilders.standaloneSetup(new DocumentUploadController(service))
            .setControllerAdvice(new GlobalExceptionHandler()).build();
    }
    @Test void createsDocumentFromMultipartUpload() throws Exception {
        when(service.upload(any(), any())).thenReturn(new DocumentResponse(5L, "Demo", "demo.pdf", null, null, null, null, null, java.util.List.of()));
        mvc.perform(multipart("/api/documents/upload").file(pdf).param("title", "Demo"))
            .andExpect(status().isCreated()).andExpect(header().string("Location", "/api/documents/5"));
    }
    @Test void rejectsBlankTitle() throws Exception {
        mvc.perform(multipart("/api/documents/upload").file(pdf).param("title", " "))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void rejectsNegativeCategory() throws Exception {
        mvc.perform(multipart("/api/documents/upload").file(pdf).param("title", "Demo").param("categoryId", "-1"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void rejectsMissingFile() throws Exception {
        mvc.perform(multipart("/api/documents/upload").param("title", "Demo"))
            .andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
    @Test void queueFailureReturns503() throws Exception {
        when(service.upload(any(), any())).thenThrow(new DocumentPublishException("internal details"));
        mvc.perform(multipart("/api/documents/upload").file(pdf).param("title", "Demo"))
            .andExpect(status().isServiceUnavailable()).andExpect(jsonPath("$.trace").doesNotExist());
    }
    @Test void invalidPdfReturns400() throws Exception {
        when(service.upload(any(), any())).thenThrow(new InvalidDocumentException("PDF signature"));
        mvc.perform(multipart("/api/documents/upload").file(pdf).param("title", "Demo"))
            .andExpect(status().isBadRequest());
    }
}
