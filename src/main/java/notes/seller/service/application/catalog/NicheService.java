package notes.seller.service.application.catalog;

import java.util.List;
import java.util.UUID;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class NicheService {
	private final NicheRepository nicheRepository;

	public NicheService(NicheRepository nicheRepository) {
		this.nicheRepository = nicheRepository;
	}

	public List<NicheEntity> listAll() {
		return nicheRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
	}

	public NicheEntity getById(UUID id) {
		return nicheRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
	}

	public NicheEntity create(String slug, String name, UUID parentId) {
		if (nicheRepository.findBySlug(slug).isPresent()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Niche slug already exists");
		}
		NicheEntity niche = new NicheEntity();
		niche.setSlug(slug);
		niche.setName(name);
		if (parentId != null) {
			niche.setParent(getById(parentId));
		}
		return nicheRepository.save(niche);
	}

	public NicheEntity update(UUID id, String slug, String name, UUID parentId) {
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
		if (parentId != null) {
			niche.setParent(getById(parentId));
		}
		return nicheRepository.save(niche);
	}
}