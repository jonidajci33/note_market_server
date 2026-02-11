package notes.seller.service.security;

import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public final class SecurityUtils {
	private SecurityUtils() {
	}

	public static UUID getUserId(Authentication authentication) {
		if (authentication == null) {
			throw new IllegalStateException("Missing authentication");
		}
		Object principal = authentication.getPrincipal();
		if (principal instanceof Jwt jwt) {
			return UUID.fromString(jwt.getSubject());
		}
		if (principal instanceof UserPrincipal userPrincipal) {
			return userPrincipal.getId();
		}
		throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
	}
}