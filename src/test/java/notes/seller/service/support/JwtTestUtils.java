package notes.seller.service.support;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.UUID;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class JwtTestUtils {
	private JwtTestUtils() {
	}

	public static RequestPostProcessor jwtWithRole(UUID userId, String role) {
		return jwt().jwt(jwt -> jwt.subject(userId.toString()))
				.authorities(new SimpleGrantedAuthority("ROLE_" + role));
	}
}
