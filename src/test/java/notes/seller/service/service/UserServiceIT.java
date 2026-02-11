package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;

import notes.seller.service.application.identity.UserService;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class UserServiceIT extends AbstractPostgresIT {
	@Autowired
	private UserService userService;
	@Autowired
	private UserRepository userRepository;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void getById_shouldReturnUser() {
		UserEntity user = userRepository.save(dataFactory.aClientUser());

		UserEntity loaded = userService.getById(user.getId());

		assertThat(loaded.getEmail()).isEqualTo(user.getEmail());
	}
}
