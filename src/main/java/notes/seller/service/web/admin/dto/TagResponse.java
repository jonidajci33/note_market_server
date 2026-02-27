package notes.seller.service.web.admin.dto;

import java.util.UUID;

public record TagResponse(UUID id, String slug, String name) {
}
