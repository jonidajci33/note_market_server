package notes.seller.service.application.delivery;

import java.util.UUID;
import notes.seller.service.application.commerce.EntitlementService;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.integration.storage.PresignedUrl;
import notes.seller.service.integration.storage.StorageDownloadRequest;
import notes.seller.service.integration.storage.StorageService;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.catalog.NoteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class DownloadService {
	private final NoteRepository noteRepository;
	private final EntitlementService entitlementService;
	private final StorageService storageService;

	public DownloadService(NoteRepository noteRepository, EntitlementService entitlementService, StorageService storageService) {
		this.noteRepository = noteRepository;
		this.entitlementService = entitlementService;
		this.storageService = storageService;
	}

	public PresignedUrl createDownloadUrl(UUID clientId, UUID noteId) {
		NoteEntity note = noteRepository.findById(noteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Note not found"));
		if (!isEntitled(clientId, note)) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not entitled to this note");
		}
		if (note.getFileKey() == null || note.getFileKey().isBlank()) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Note file is not uploaded yet");
		}
		return storageService.createDownloadUrl(new StorageDownloadRequest(note.getFileKey()));
	}

	private boolean isEntitled(UUID clientId, NoteEntity note) {
		if (entitlementService.hasEntitlement(clientId, ItemType.NOTE, note.getId())) {
			return true;
		}
		if (note.getCourse() != null) {
			return entitlementService.hasEntitlement(clientId, ItemType.COURSE, note.getCourse().getId());
		}
		return false;
	}
}