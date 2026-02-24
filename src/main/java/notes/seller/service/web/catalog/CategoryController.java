package notes.seller.service.web.catalog;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.CategoryService;
import notes.seller.service.persistence.catalog.CategoryEntity;
import notes.seller.service.web.catalog.dto.CategoryRequest;
import notes.seller.service.web.catalog.dto.CategoryResponse;
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
@RequestMapping("/api/v1/categories")
public class CategoryController {
	private final CategoryService categoryService;

	public CategoryController(CategoryService categoryService) {
		this.categoryService = categoryService;
	}

	@GetMapping
	public List<CategoryResponse> list() {
		return categoryService.listAll().stream().map(this::toResponse).toList();
	}

	@PostMapping
	@PreAuthorize("hasRole('SYSADMIN')")
	public CategoryResponse create(@Valid @RequestBody CategoryRequest request) {
		CategoryEntity category = categoryService.create(request.slug(), request.name());
		return toResponse(category);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SYSADMIN')")
	public CategoryResponse update(@PathVariable("id") UUID id, @Valid @RequestBody CategoryRequest request) {
		CategoryEntity category = categoryService.update(id, request.slug(), request.name());
		return toResponse(category);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('SYSADMIN')")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable("id") UUID id) {
		categoryService.delete(id);
	}

	private CategoryResponse toResponse(CategoryEntity category) {
		return new CategoryResponse(
				category.getId(),
				category.getSlug(),
				category.getName()
		);
	}
}
