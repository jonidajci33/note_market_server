package notes.seller.service.web.identity.dto;

import java.util.Set;
import java.util.UUID;
import notes.seller.service.domain.identity.Role;

public record UserResponse(
		UUID id,
		String email,
		Set<Role> roles,
		SellerProfileResponse sellerProfile
) {
}