package notes.seller.service.web.advice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import notes.seller.service.common.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiError> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
		HttpStatus status = HttpStatus.valueOf(ex.getStatusCode().value());
		String message = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
		log.warn("Handled API error: status={} path={} correlationId={} message={}",
				status.value(), request.getRequestURI(), correlationId(), message);
		ApiError error = build(status, message, List.of(), request);
		return ResponseEntity.status(status).body(error);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
		List<String> details = ex.getBindingResult().getFieldErrors().stream()
				.map(this::formatFieldError)
				.collect(Collectors.toList());
		log.warn("Validation failed: path={} correlationId={} errors={}", request.getRequestURI(), correlationId(), details.size());
		ApiError error = build(HttpStatus.BAD_REQUEST, "Validation failed", details, request);
		return ResponseEntity.badRequest().body(error);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		log.warn("Access denied: path={} correlationId={}", request.getRequestURI(), correlationId());
		ApiError error = build(HttpStatus.FORBIDDEN, "Access denied", List.of(), request);
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
	}

	@ExceptionHandler(IllegalStateException.class)
	public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
		log.warn("Service unavailable: path={} correlationId={} message={}",
				request.getRequestURI(), correlationId(), ex.getMessage());
		ApiError error = build(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), List.of(), request);
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiError> handleGeneric(Exception ex, HttpServletRequest request) {
		log.error("Unhandled exception: path={} correlationId={}", request.getRequestURI(), correlationId(), ex);
		ApiError error = build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", List.of(), request);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}

	private ApiError build(HttpStatus status, String message, List<String> details, HttpServletRequest request) {
		String correlationId = MDC.get("correlationId");
		return new ApiError(
				Instant.now(),
				status.value(),
				status.getReasonPhrase(),
				message,
				request.getRequestURI(),
				correlationId,
				details
		);
	}

	private String formatFieldError(FieldError error) {
		return error.getField() + ": " + error.getDefaultMessage();
	}

	private String correlationId() {
		String value = MDC.get("correlationId");
		if (value == null || value.isBlank()) {
			return "n/a";
		}
		return value;
	}
}
