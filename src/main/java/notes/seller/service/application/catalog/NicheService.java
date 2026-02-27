package notes.seller.service.application.catalog;

import java.util.List;
import java.util.UUID;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.catalog.NoteRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NicheService {
	private final NicheRepository nicheRepository;
	private final CategoryService categoryService;
	private final NoteRepository noteRepository;

	public NicheService(NicheRepository nicheRepository, CategoryService categoryService,
						NoteRepository noteRepository) {
		this.nicheRepository = nicheRepository;
		this.categoryService = categoryService;
		this.noteRepository = noteRepository;
	}

	public List<NicheEntity> listAll() {
		return nicheRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}

	public List<NicheEntity> listByCategory(UUID categoryId) {
		return nicheRepository.findByCategoryId(categoryId);
	}

	public NicheEntity getById(UUID id) {
		return nicheRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
	}

	public NicheEntity create(String slug, String name, UUID categoryId) {
		if (nicheRepository.findBySlug(slug).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Niche slug already exists");
		}
		NicheEntity niche = new NicheEntity();
		niche.setSlug(slug);
		niche.setName(name);
		niche.setCategory(categoryService.getById(categoryId));
		return nicheRepository.save(niche);
	}

	public NicheEntity update(UUID id, String slug, String name, UUID categoryId) {
		NicheEntity niche = getById(id);
		if (slug != null && !slug.isBlank() && !slug.equals(niche.getSlug())) {
			if (nicheRepository.findBySlug(slug).isPresent()) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Niche slug already exists");
			}
			niche.setSlug(slug);
		}
		if (name != null && !name.isBlank()) {
			niche.setName(name);
		}
		if (categoryId != null) {
			niche.setCategory(categoryService.getById(categoryId));
		}
		return nicheRepository.save(niche);
	}

	public void delete(UUID id) {
		NicheEntity niche = getById(id);
		if (noteRepository.existsByNicheId(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete niche with associated notes");
		}
		nicheRepository.delete(niche);
	}
}
