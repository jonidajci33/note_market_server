package notes.seller.service.web.identity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterSellerRequest(
		@Email @NotBlank String email,
		@NotBlank String password,
		String displayName,
		String bio
) {
}