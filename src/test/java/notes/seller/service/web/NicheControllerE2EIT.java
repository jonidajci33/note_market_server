package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import java.util.UUID;
import notes.seller.service.persistence.catalog.NicheRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.web.catalog.dto.NicheRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class NicheControllerE2EIT extends AbstractPostgresIT {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private NicheRepository nicheRepository;

	@Test
	void create_shouldPersistNiche() throws Exception {
		NicheRequest request = new NicheRequest("math", "Math", null);

		mockMvc.perform(post("/api/v1/niches")
						.with(jwtWithRole(UUID.randomUUID(), "SYSADMIN"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.slug").value("math"));

		assertThat(nicheRepository.findBySlug("math")).isPresent();
	}
}
