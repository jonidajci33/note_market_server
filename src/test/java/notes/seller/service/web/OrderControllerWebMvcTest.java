package notes.seller.service.web;

import static notes.seller.service.support.JwtTestUtils.jwtWithRole;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import notes.seller.service.application.commerce.OrderService;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.domain.commerce.OrderStatus;
import notes.seller.service.persistence.commerce.OrderEntity;
import notes.seller.service.persistence.commerce.OrderItemEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.security.JwtProperties;
import notes.seller.service.security.SecurityConfig;
import notes.seller.service.security.UserDetailsServiceImpl;
import notes.seller.service.web.advice.GlobalExceptionHandler;
import notes.seller.service.web.commerce.OrderController;
import notes.seller.service.web.commerce.dto.OrderCreateRequest;
import notes.seller.service.web.commerce.dto.OrderItemRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@EnableConfigurationProperties(JwtProperties.class)
class OrderControllerWebMvcTest {
	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private ObjectMapper objectMapper;
	@MockitoBean
	private OrderService orderService;
	@MockitoBean
	private UserDetailsServiceImpl userDetailsService;

	@Test
	void create_shouldAllowClient() throws Exception {
		UUID clientId = UUID.randomUUID();
		UUID itemId = UUID.randomUUID();
		OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(ItemType.NOTE, itemId)));
		OrderEntity order = new OrderEntity();
		order.setId(UUID.randomUUID());
		order.setStatus(OrderStatus.PENDING);
		order.setCurrency("USD");
		order.setTotalAmount(new BigDecimal("9.99"));
		order.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));
		UserEntity client = new UserEntity();
		client.setId(clientId);
		order.setClient(client);
		OrderItemEntity item = new OrderItemEntity();
		item.setItemType(ItemType.NOTE);
		item.setItemId(itemId);
		item.setUnitPrice(new BigDecimal("9.99"));
		order.setItems(List.of(item));
		when(orderService.createOrder(any(), any())).thenReturn(order);

		mockMvc.perform(post("/api/v1/orders")
						.with(jwtWithRole(clientId, "CLIENT"))
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	void create_shouldRejectUnauthorized() throws Exception {
		OrderCreateRequest request = new OrderCreateRequest(List.of(new OrderItemRequest(ItemType.NOTE, UUID.randomUUID())));

		mockMvc.perform(post("/api/v1/orders")
						.contentType(APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(request)))
				.andExpect(status().isUnauthorized());
	}
}
