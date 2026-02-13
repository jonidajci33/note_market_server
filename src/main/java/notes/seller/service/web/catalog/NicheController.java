package notes.seller.service.web.catalog;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.NicheService;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.web.catalog.dto.NicheRequest;
import notes.seller.service.web.catalog.dto.NicheResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/niches")
public class NicheController {
	private final NicheService nicheService;

	public NicheController(NicheService nicheService) {
		this.nicheService = nicheService;
	}

	@GetMapping
	public List<NicheResponse> list(@RequestParam(required = false) UUID categoryId) {
		List<NicheEntity> niches = categoryId != null
				? nicheService.listByCategory(categoryId)
				: nicheService.listAll();
		return niches.stream().map(this::toResponse).toList();
	}

	@PostMapping
	@PreAuthorize("hasRole('SYSADMIN')")
	public NicheResponse create(@Valid @RequestBody NicheRequest request) {
		NicheEntity niche = nicheService.create(request.slug(), request.name(), request.categoryId());
		return toResponse(niche);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SYSADMIN')")
	public NicheResponse update(@PathVariable("id") UUID id, @Valid @RequestBody NicheRequest request) {
		NicheEntity niche = nicheService.update(id, request.slug(), request.name(), request.categoryId());
		return toResponse(niche);
	}

	private NicheResponse toResponse(NicheEntity niche) {
		return new NicheResponse(
				niche.getId(),
				niche.getSlug(),
				niche.getName(),
				niche.getCategory().getId()
		);
	}
}
