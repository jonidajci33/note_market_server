package notes.seller.service.web.commerce;

import java.util.List;
import java.util.UUID;
import notes.seller.service.application.commerce.EntitlementService;
import notes.seller.service.persistence.commerce.EntitlementEntity;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.commerce.dto.LibraryItemResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
public class LibraryController {
	private final EntitlementService entitlementService;

	public LibraryController(EntitlementService entitlementService) {
		this.entitlementService = entitlementService;
	}

	@GetMapping("/library")
	public List<LibraryItemResponse> library(Authentication authentication) {
		UUID clientId = SecurityUtils.getUserId(authentication);
		return entitlementService.listForClient(clientId).stream().map(this::toResponse).toList();
	}

	private LibraryItemResponse toResponse(EntitlementEntity entitlement) {
		return new LibraryItemResponse(
				entitlement.getId(),
				entitlement.getItemType(),
				entitlement.getItemId(),
				entitlement.getGrantedAt()
		);
	}
}
