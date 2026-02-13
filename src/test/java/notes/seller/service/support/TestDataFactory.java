package notes.seller.service.support;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.datafaker.Faker;
import notes.seller.service.domain.catalog.CourseStatus;
import notes.seller.service.domain.catalog.NoteStatus;
import notes.seller.service.domain.commerce.ItemType;
import notes.seller.service.domain.commerce.OrderStatus;
import notes.seller.service.domain.commerce.PaymentProvider;
import notes.seller.service.domain.commerce.PaymentStatus;
import notes.seller.service.domain.identity.Role;
import notes.seller.service.domain.identity.UserStatus;
import notes.seller.service.persistence.catalog.CourseEntity;
import notes.seller.service.persistence.catalog.NicheEntity;
import notes.seller.service.persistence.catalog.NoteEntity;
import notes.seller.service.persistence.commerce.EntitlementEntity;
import notes.seller.service.persistence.commerce.OrderEntity;
import notes.seller.service.persistence.commerce.OrderItemEntity;
import notes.seller.service.persistence.commerce.PaymentEntity;
import notes.seller.service.persistence.identity.SellerProfileEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.web.catalog.dto.CourseCreateRequest;
import notes.seller.service.web.catalog.dto.NicheRequest;
import notes.seller.service.web.catalog.dto.NoteCreateRequest;
import notes.seller.service.web.commerce.dto.OrderCreateRequest;
import notes.seller.service.web.commerce.dto.OrderItemRequest;
import notes.seller.service.web.identity.dto.LoginRequest;
import notes.seller.service.web.identity.dto.RegisterRequest;
import notes.seller.service.web.identity.dto.RegisterSellerRequest;

public final class TestDataFactory {
	private static final BigDecimal DEFAULT_PRICE = new BigDecimal("19.99");
	private static final String DEFAULT_CURRENCY = "USD";
	private static final Instant DEFAULT_INSTANT = Instant.parse("2024-01-01T00:00:00Z");

	private final Faker faker;
	private final AtomicInteger sequence = new AtomicInteger(1);

	public TestDataFactory(Faker faker) {
		this.faker = faker;
	}

	public static TestDataFactory seeded() {
		return new TestDataFactory(FakerFactory.seeded());
	}

	public UserEntity aClientUser() {
		return aUserWithRoles(Role.CLIENT);
	}

	public UserEntity aSellerUser() {
		UserEntity user = aUserWithRoles(Role.SELLER);
		SellerProfileEntity profile = aSellerProfile(user);
		user.setSellerProfile(profile);
		return user;
	}

	public UserEntity aSysadminUser() {
		return aUserWithRoles(Role.SYSADMIN);
	}

	public UserEntity aUserWithRoles(Role... roles) {
		UserEntity user = new UserEntity();
		int seed = next();
		user.setEmail("user" + seed + "@example.com");
		user.setPasswordHash("hash-" + seed);
		user.setStatus(UserStatus.ACTIVE);
		setRoles(user, roles);
		return user;
	}

	public SellerProfileEntity aSellerProfile(UserEntity user) {
		SellerProfileEntity profile = new SellerProfileEntity();
		profile.setUser(user);
		profile.setDisplayName("Seller " + next());
		profile.setBio("Bio " + faker.lorem().sentence());
		profile.setPayoutInfo("payout-" + next());
		return profile;
	}

	public NicheEntity aNiche() {
		NicheEntity niche = new NicheEntity();
		int seed = next();
		niche.setSlug("niche-" + seed);
		niche.setName("Niche " + seed);
		return niche;
	}

	public CourseEntity aCourse(UserEntity seller, NicheEntity niche) {
		CourseEntity course = new CourseEntity();
		course.setSeller(seller);
		course.setNiche(niche);
		course.setTitle("Course " + next());
		course.setDescription(faker.lorem().sentence());
		course.setPrice(DEFAULT_PRICE);
		course.setStatus(CourseStatus.DRAFT);
		return course;
	}

	public NoteEntity aNote(UserEntity seller, NicheEntity niche) {
		NoteEntity note = new NoteEntity();
		note.setSeller(seller);
		note.setNiche(niche);
		note.setTitle("Note " + next());
		note.setDescription(faker.lorem().sentence());
		note.setPrice(DEFAULT_PRICE);
		note.setStatus(NoteStatus.DRAFT);
		note.setTags(new HashSet<>(Set.of("tag-" + next(), "tag-" + next())));
		return note;
	}

	public OrderEntity anOrder(UserEntity client) {
		OrderEntity order = new OrderEntity();
		order.setClient(client);
		order.setStatus(OrderStatus.PENDING);
		order.setCurrency(DEFAULT_CURRENCY);
		order.setTotalAmount(DEFAULT_PRICE);
		return order;
	}

	public OrderEntity anOrderWithSingleItem(UserEntity client, ItemType itemType, UUID itemId) {
		OrderEntity order = anOrder(client);
		OrderItemEntity item = anOrderItem(order, itemType, itemId, DEFAULT_PRICE);
		order.getItems().add(item);
		order.setTotalAmount(DEFAULT_PRICE);
		return order;
	}

	public OrderItemEntity anOrderItem(OrderEntity order, ItemType itemType, UUID itemId, BigDecimal unitPrice) {
		OrderItemEntity item = new OrderItemEntity();
		item.setOrder(order);
		item.setItemType(itemType);
		item.setItemId(itemId);
		item.setUnitPrice(unitPrice);
		return item;
	}

	public PaymentEntity aPayment(OrderEntity order) {
		PaymentEntity payment = new PaymentEntity();
		payment.setOrder(order);
		payment.setProvider(PaymentProvider.STUB);
		payment.setStatus(PaymentStatus.PAID);
		payment.setProviderRef("pay-" + next());
		payment.setAmount(order.getTotalAmount());
		return payment;
	}

	public EntitlementEntity anEntitlement(UserEntity client, ItemType itemType, UUID itemId) {
		EntitlementEntity entitlement = new EntitlementEntity();
		entitlement.setClient(client);
		entitlement.setItemType(itemType);
		entitlement.setItemId(itemId);
		entitlement.setGrantedAt(DEFAULT_INSTANT);
		return entitlement;
	}

	public NicheRequest aNicheRequest() {
		int seed = next();
		return new NicheRequest("niche-" + seed, "Niche " + seed, UUID.fromString("00000000-0000-0000-0000-00000000bb01"));
	}

	public CourseCreateRequest aCourseCreateRequest(UUID nicheId) {
		return new CourseCreateRequest(nicheId, "Course " + next(), faker.lorem().sentence(), DEFAULT_PRICE);
	}

	public NoteCreateRequest aNoteCreateRequest(UUID nicheId) {
		return new NoteCreateRequest(nicheId, null, "Note " + next(), faker.lorem().sentence(), DEFAULT_PRICE, Set.of("tag-" + next()));
	}

	public OrderCreateRequest anOrderCreateRequest(ItemType itemType, UUID itemId) {
		return new OrderCreateRequest(List.of(new OrderItemRequest(itemType, itemId)));
	}

	public RegisterRequest aRegisterRequest() {
		int seed = next();
		return new RegisterRequest("user" + seed + "@example.com", "Password1!");
	}

	public RegisterSellerRequest aRegisterSellerRequest() {
		int seed = next();
		return new RegisterSellerRequest("seller" + seed + "@example.com", "Password1!", "Seller " + seed, "Bio " + seed);
	}

	public LoginRequest aLoginRequest(String email) {
		return new LoginRequest(email, "Password1!");
	}

	private void setRoles(UserEntity user, Role... roles) {
		if (user.getRoles() == null) {
			user.setRoles(new HashSet<>());
		} else {
			user.getRoles().clear();
		}
		if (roles == null || roles.length == 0) {
			user.getRoles().add(Role.CLIENT);
			return;
		}
		user.getRoles().addAll(Arrays.asList(roles));
	}

	private int next() {
		return sequence.getAndIncrement();
	}
}
