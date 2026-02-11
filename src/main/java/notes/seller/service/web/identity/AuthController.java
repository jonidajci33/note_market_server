package notes.seller.service.web.identity;

import jakarta.validation.Valid;
import notes.seller.service.application.identity.AuthResult;
import notes.seller.service.application.identity.AuthService;
import notes.seller.service.persistence.identity.SellerProfileEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.web.identity.dto.AuthResponse;
import notes.seller.service.web.identity.dto.LoginRequest;
import notes.seller.service.web.identity.dto.RegisterRequest;
import notes.seller.service.web.identity.dto.RegisterSellerRequest;
import notes.seller.service.web.identity.dto.SellerProfileResponse;
import notes.seller.service.web.identity.dto.UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	private final AuthService authService;
	private final boolean openSellerRegistration;

	public AuthController(AuthService authService,
					  @Value("${app.security.open-seller-registration:true}") boolean openSellerRegistration) {
		this.authService = authService;
		this.openSellerRegistration = openSellerRegistration;
	}

	@PostMapping("/register")
	public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
		AuthResult result = authService.registerClient(request.email(), request.password());
		return toAuthResponse(result);
	}

	@PostMapping("/register-seller")
	public AuthResponse registerSeller(@Valid @RequestBody RegisterSellerRequest request, Authentication authentication) {
		if (!openSellerRegistration && !isAdmin(authentication)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Seller registration is closed");
		}
		AuthResult result = authService.registerSeller(request.email(), request.password(), request.displayName(), request.bio());
		return toAuthResponse(result);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		AuthResult result = authService.login(request.email(), request.password());
		return toAuthResponse(result);
	}

	private boolean isAdmin(Authentication authentication) {
		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			if ("ROLE_SYSADMIN".equals(authority.getAuthority())) {
				return true;
			}
		}
		return false;
	}

	private AuthResponse toAuthResponse(AuthResult result) {
		return new AuthResponse(result.token(), toUserResponse(result.user()));
	}

	private UserResponse toUserResponse(UserEntity user) {
		SellerProfileResponse profile = null;
		SellerProfileEntity sellerProfile = user.getSellerProfile();
		if (sellerProfile != null) {
			profile = new SellerProfileResponse(sellerProfile.getDisplayName(), sellerProfile.getBio());
		}
		return new UserResponse(user.getId(), user.getEmail(), user.getRoles(), profile);
	}
}