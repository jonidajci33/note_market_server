package notes.seller.service.repository;

import static notes.seller.service.support.DbAssertions.assertPersisted;
import static org.assertj.core.api.Assertions.assertThat;

import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryIT extends AbstractPostgresIT {
	@Autowired
	private UserRepository userRepository;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void save_shouldPersist_andFindByEmailIgnoreCase() {
		UserEntity user = dataFactory.aClientUser();

		UserEntity saved = userRepository.save(user);

		assertPersisted(saved);
		assertThat(userRepository.findByEmailIgnoreCase(saved.getEmail().toUpperCase()))
				.isPresent()
				.get()
				.extracting(UserEntity::getEmail)
				.isEqualTo(saved.getEmail());
	}

	@Test
	void existsByEmailIgnoreCase_shouldReturnTrue_whenExists() {
		UserEntity user = dataFactory.aClientUser();
		userRepository.save(user);

		assertThat(userRepository.existsByEmailIgnoreCase(user.getEmail().toUpperCase())).isTrue();
	}
}
