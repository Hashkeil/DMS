package at.fhtw.paperless.rest.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({org.springframework.web.method.annotation.HandlerMethodValidationException.class, InvalidDocumentException.class, jakarta.validation.ConstraintViolationException.class,
            org.springframework.web.bind.MissingServletRequestParameterException.class,
            org.springframework.web.multipart.support.MissingServletRequestPartException.class})
    public ResponseEntity<ErrorResponse> handleInvalidUpload(Exception failure, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Invalid upload: check PDF, title and category", request);
    }

    @ExceptionHandler(org.springframework.web.multipart.MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleTooLarge(HttpServletRequest request) {
        return error(HttpStatus.PAYLOAD_TOO_LARGE, "PDF exceeds the upload size limit (10 MiB)", request);
    }

    @ExceptionHandler(DocumentPublishException.class)
    public ResponseEntity<ErrorResponse> handlePublish(DocumentPublishException failure, HttpServletRequest request) {
        log.error("OCR publish failed", failure);
        return error(HttpStatus.SERVICE_UNAVAILABLE, "OCR queue unavailable; upload was not completed", request);
    }

    @ExceptionHandler(DocumentStorageException.class)
    public ResponseEntity<ErrorResponse> handleStorage(DocumentStorageException failure, HttpServletRequest request) {
        log.error("PDF storage failed", failure);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "Could not store document", request);
    }

    @ExceptionHandler({DocumentNotFoundException.class, CategoryNotFoundException.class,
            LabelNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RuntimeException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, exception.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception,
                                                          HttpServletRequest request) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(field -> field.getField() + ": " + field.getDefaultMessage())
                .sorted().collect(Collectors.joining("; "));
        return error(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or contains invalid JSON or field values", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "Path parameter must be a valid numeric identifier", request);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, CategoryInUseException.class})
    public ResponseEntity<ErrorResponse> handleConflict(HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, "Operation conflicts with existing data. A category in use cannot be deleted.", request);
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message, HttpServletRequest request) {
        log.warn("Request failed: status={}, path={}", status.value(), request.getRequestURI());
        return ResponseEntity.status(status).body(new ErrorResponse(
                LocalDateTime.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI()));
    }
}
