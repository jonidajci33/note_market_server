package notes.seller.service.web.admin;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.TagService;
import notes.seller.service.persistence.catalog.TagEntity;
import notes.seller.service.web.admin.dto.TagRequest;
import notes.seller.service.web.admin.dto.TagResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/tags")
@PreAuthorize("hasRole('SYSADMIN')")
public class AdminTagController {
	private final TagService tagService;

	public AdminTagController(TagService tagService) {
		this.tagService = tagService;
	}

	@GetMapping
	public List<TagResponse> list() {
		return tagService.listAll().stream().map(this::toResponse).toList();
	}

	@GetMapping("/{id}")
	public TagResponse getById(@PathVariable("id") UUID id) {
		return toResponse(tagService.getById(id));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TagResponse create(@Valid @RequestBody TagRequest request) {
		TagEntity tag = tagService.create(request.slug(), request.name());
		return toResponse(tag);
	}

	@PutMapping("/{id}")
	public TagResponse update(@PathVariable("id") UUID id, @Valid @RequestBody TagRequest request) {
		TagEntity tag = tagService.update(id, request.slug(), request.name());
		return toResponse(tag);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") UUID id) {
		tagService.delete(id);
	}

	@PostMapping("/{id}/niches/{nicheId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void addNicheAssociation(@PathVariable("id") UUID id, @PathVariable("nicheId") UUID nicheId) {
		tagService.addNicheAssociation(id, nicheId);
	}

	@DeleteMapping("/{id}/niches/{nicheId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void removeNicheAssociation(@PathVariable("id") UUID id, @PathVariable("nicheId") UUID nicheId) {
		tagService.removeNicheAssociation(id, nicheId);
	}

	private TagResponse toResponse(TagEntity tag) {
		return new TagResponse(tag.getId(), tag.getSlug(), tag.getName());
	}
}
