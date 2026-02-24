package notes.seller.service.application.catalog;

import java.util.List;
import java.util.UUID;
import notes.seller.service.persistence.catalog.CategoryEntity;
import notes.seller.service.persistence.catalog.CategoryRepository;
import notes.seller.service.persistence.catalog.NicheRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class CategoryService {
	private final CategoryRepository categoryRepository;
	private final NicheRepository nicheRepository;

	public CategoryService(CategoryRepository categoryRepository, NicheRepository nicheRepository) {
		this.categoryRepository = categoryRepository;
		this.nicheRepository = nicheRepository;
	}

	public List<CategoryEntity> listAll() {
		return categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}

	public CategoryEntity getById(UUID id) {
		return categoryRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
	}

	public CategoryEntity create(String slug, String name) {
		if (categoryRepository.findBySlug(slug).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Category slug already exists");
		}
		CategoryEntity category = new CategoryEntity();
		category.setSlug(slug);
		category.setName(name);
		return categoryRepository.save(category);
	}

	public CategoryEntity update(UUID id, String slug, String name) {
		CategoryEntity category = getById(id);
		if (slug != null && !slug.isBlank() && !slug.equals(category.getSlug())) {
			if (categoryRepository.findBySlug(slug).isPresent()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Category slug already exists");
			}
			category.setSlug(slug);
		}
		if (name != null && !name.isBlank()) {
			category.setName(name);
		}
		return categoryRepository.save(category);
	}

	public void delete(UUID id) {
		CategoryEntity category = getById(id);
		if (nicheRepository.existsByCategoryId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete category with associated niches");
		}
		categoryRepository.delete(category);
	}
}
