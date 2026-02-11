package notes.seller.service.web.delivery;

import java.util.UUID;
import notes.seller.service.application.delivery.DownloadService;
import notes.seller.service.integration.storage.PresignedUrl;
import notes.seller.service.security.SecurityUtils;
import notes.seller.service.web.delivery.dto.DownloadUrlResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notes")
public class DownloadController {
	private final DownloadService downloadService;

	public DownloadController(DownloadService downloadService) {
		this.downloadService = downloadService;
	}

	@PostMapping("/{id}/download")
	public DownloadUrlResponse download(@PathVariable("id") UUID id, Authentication authentication) {
		UUID clientId = SecurityUtils.getUserId(authentication);
		PresignedUrl url = downloadService.createDownloadUrl(clientId, id);
		return new DownloadUrlResponse(url.url(), url.expiresAt());
	}
}
