package notes.seller.service.application.catalog;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.persistence.catalog.TagEntity;
import notes.seller.service.persistence.catalog.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TagService {
	private final TagRepository tagRepository;
	private final NicheRepository nicheRepository;

	public TagService(TagRepository tagRepository, NicheRepository nicheRepository) {
		this.tagRepository = tagRepository;
		this.nicheRepository = nicheRepository;
	}

	@Transactional(readOnly = true)
	public List<TagEntity> listAll() {
		return tagRepository.findAllByOrderByNameAsc();
	}

	@Transactional(readOnly = true)
	public List<TagEntity> listByNiche(UUID nicheId) {
		return tagRepository.findByNicheId(nicheId);
	}

	@Transactional(readOnly = true)
	public TagEntity getById(UUID id) {
		return tagRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tag not found"));
	}

	public TagEntity create(String slug, String name) {
		if (tagRepository.existsBySlug(slug)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag slug already exists");
		}
		TagEntity tag = new TagEntity();
		tag.setSlug(slug);
		tag.setName(name);
		return tagRepository.save(tag);
	}

	public TagEntity update(UUID id, String slug, String name) {
		TagEntity tag = getById(id);
		if (slug != null && !slug.isBlank() && !slug.equals(tag.getSlug())) {
			if (tagRepository.existsBySlug(slug)) {
				throw new ResponseStatusException(HttpStatus.CONFLICT, "Tag slug already exists");
			}
			tag.setSlug(slug);
		}
		if (name != null && !name.isBlank()) {
			tag.setName(name);
		}
		return tagRepository.save(tag);
	}

	public void delete(UUID id) {
		TagEntity tag = getById(id);
		if (tagRepository.existsNoteWithTag(id)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot delete tag with associated notes");
		}
		tagRepository.delete(tag);
	}

	public void addNicheAssociation(UUID tagId, UUID nicheId) {
		TagEntity tag = getById(tagId);
		NicheEntity niche = nicheRepository.findWithTagsById(nicheId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
		niche.getTags().add(tag);
		nicheRepository.save(niche);
	}

	public void removeNicheAssociation(UUID tagId, UUID nicheId) {
		TagEntity tag = getById(tagId);
		NicheEntity niche = nicheRepository.findWithTagsById(nicheId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Niche not found"));
		niche.getTags().remove(tag);
		nicheRepository.save(niche);
	}

	@Transactional(readOnly = true)
	public Set<TagEntity> resolveTagIds(Set<UUID> tagIds) {
		if (tagIds == null || tagIds.isEmpty()) {
			return new HashSet<>();
		}
		Set<TagEntity> tags = tagRepository.findByIdIn(tagIds);
		if (tags.size() != tagIds.size()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more tag IDs not found");
		}
		return tags;
	}
}
