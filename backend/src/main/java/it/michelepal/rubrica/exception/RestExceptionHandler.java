package it.michelepal.rubrica.exception;

import it.michelepal.rubrica.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@SuppressWarnings("null")
public class RestExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(RestExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiErrorResponse> validation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
            .forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validazione richiesta fallita: method={}, path={}, fields={}", request.getMethod(), request.getRequestURI(), fields);
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Alcuni campi non sono validi.", request, fields);
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiErrorResponse> notFound(NotFoundException exception, HttpServletRequest request) {
        log.warn("Risorsa non trovata: method={}, path={}, message={}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return error(HttpStatus.NOT_FOUND, "NOT_FOUND", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiErrorResponse> conflict(ConflictException exception, HttpServletRequest request) {
        log.warn("Conflitto richiesta: method={}, path={}, message={}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return error(HttpStatus.CONFLICT, "CONFLICT", exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiErrorResponse> badCredentials(BadCredentialsException exception, HttpServletRequest request) {
        log.warn("Tentativo login fallito: method={}, path={}", request.getMethod(), request.getRequestURI());
        return error(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Credenziali non valide.", request, Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiErrorResponse> generic(Exception exception, HttpServletRequest request) {
        log.error("Errore applicativo non gestito: method={}, path={}", request.getMethod(), request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Si è verificato un errore imprevisto.", request, Map.of());
    }

    private ResponseEntity<ApiErrorResponse> error(
        HttpStatus status,
        String code,
        String message,
        HttpServletRequest request,
        Map<String, String> fieldErrors
    ) {
        HttpStatusCode statusCode = Objects.requireNonNull(status);
        return ResponseEntity.status(statusCode).body(new ApiErrorResponse(
            Instant.now(),
            status.value(),
            code,
            message,
            request.getRequestURI(),
            fieldErrors
        ));
    }
}
