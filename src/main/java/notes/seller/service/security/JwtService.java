package notes.seller.service.security;

import java.time.Instant;
import java.util.List;
import notes.seller.service.domain.identity.Role;
import notes.seller.service.persistence.identity.UserEntity;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
	private final JwtEncoder jwtEncoder;
	private final JwtProperties properties;

	public JwtService(JwtEncoder jwtEncoder, JwtProperties properties) {
		this.jwtEncoder = jwtEncoder;
		this.properties = properties;
	}

	public String generateToken(UserEntity user) {
		Instant now = Instant.now();
		List<String> roles = user.getRoles().stream().map(Role::name).toList();
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.issuer())
				.issuedAt(now)
				.expiresAt(now.plus(properties.expiration()))
				.subject(user.getId().toString())
				.claim("email", user.getEmail())
				.claim("roles", roles)
				.build();
		return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
	}
}