package notes.seller.service.application.catalog;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CatalogQueryService {
	private final NoteRepository noteRepository;

	public CatalogQueryService(NoteRepository noteRepository) {
		this.noteRepository = noteRepository;
	}

	public Page<NoteEntity> findNotes(UUID nicheId, UUID sellerId, BigDecimal minPrice, BigDecimal maxPrice,
							 String query, List<UUID> tagIds, Pageable pageable) {
		Specification<NoteEntity> spec = Specification.where(NoteSpecifications.fetchNicheAndCategory())
				.and(NoteSpecifications.hasStatus(NoteStatus.PUBLISHED));
		spec = andIfPresent(spec, NoteSpecifications.hasNiche(nicheId));
		spec = andIfPresent(spec, NoteSpecifications.hasSeller(sellerId));
		spec = andIfPresent(spec, NoteSpecifications.priceBetween(minPrice, maxPrice));
		spec = andIfPresent(spec, NoteSpecifications.matchesQuery(query));
		spec = andIfPresent(spec, NoteSpecifications.hasTags(tagIds));
		return noteRepository.findAll(spec, pageable);
	}

	public long countPublishedNotesBySeller(UUID sellerId) {
		return noteRepository.countBySellerIdAndStatus(sellerId, NoteStatus.PUBLISHED);
	}

	private static <T> Specification<T> andIfPresent(Specification<T> base, Specification<T> next) {
		if (next == null) {
			return base;
		}
		return base.and(next);
	}
}
