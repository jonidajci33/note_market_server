package notes.seller.service.web.identity;

import java.util.UUID;
import notes.seller.service.application.identity.UserService;
import notes.seller.service.persistence.identity.SellerProfileEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.identity.dto.SellerProfileResponse;
import notes.seller.service.web.identity.dto.UserResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class MeController {
	private final UserService userService;

	public MeController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping("/me")
	public UserResponse me(Authentication authentication) {
		UUID userId = SecurityUtils.getUserId(authentication);
		UserEntity user = userService.getById(userId);
		SellerProfileResponse profile = null;
		SellerProfileEntity sellerProfile = user.getSellerProfile();
		if (sellerProfile != null) {
			profile = new SellerProfileResponse(sellerProfile.getDisplayName(), sellerProfile.getBio());
		}
		return new UserResponse(user.getId(), user.getEmail(), user.getRoles(), profile);
	}
}