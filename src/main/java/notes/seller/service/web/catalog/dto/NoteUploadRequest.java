package notes.seller.service.web.catalog.dto;

import jakarta.validation.constraints.NotBlank;

public record NoteUploadRequest(
		@NotBlank String contentType,
		Long fileSize,
		String checksumSha256
) {
}