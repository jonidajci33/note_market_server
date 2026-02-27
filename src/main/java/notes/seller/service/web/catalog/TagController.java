package notes.seller.service.web.catalog;

import java.util.List;
import java.util.UUID;
import notes.seller.service.application.catalog.TagService;
import notes.seller.service.persistence.catalog.TagEntity;
import notes.seller.service.web.catalog.dto.TagInfo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {
	private final TagService tagService;

	public TagController(TagService tagService) {
		this.tagService = tagService;
	}

	@GetMapping
	public List<TagInfo> list(@RequestParam(required = false) UUID nicheId) {
		List<TagEntity> tags = nicheId != null
				? tagService.listByNiche(nicheId)
				: tagService.listAll();
		return tags.stream().map(this::toTagInfo).toList();
	}

	private TagInfo toTagInfo(TagEntity tag) {
		return new TagInfo(tag.getId(), tag.getName(), tag.getSlug());
	}
}
