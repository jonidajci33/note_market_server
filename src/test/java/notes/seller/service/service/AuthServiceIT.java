package notes.seller.service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import notes.seller.service.application.identity.AuthResult;
import notes.seller.service.application.identity.AuthService;
import notes.seller.service.domain.identity.Role;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.support.AbstractPostgresIT;
import notes.seller.service.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest
@Transactional
class AuthServiceIT extends AbstractPostgresIT {
	@Autowired
	private AuthService authService;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;

	private TestDataFactory dataFactory;

	@BeforeEach
	void setUp() {
		dataFactory = TestDataFactory.seeded();
	}

	@Test
	void registerClient_shouldPersistUserAndReturnToken() {
		var request = dataFactory.aRegisterRequest();

		AuthResult result = authService.registerClient(request.email(), request.password());

		assertThat(result.token()).isNotBlank();
		assertThat(result.user().getId()).isNotNull();
		assertThat(result.user().getRoles()).contains(Role.CLIENT);
	}

	@Test
	void registerSeller_shouldCreateSellerProfile() {
		var request = dataFactory.aRegisterSellerRequest();

		AuthResult result = authService.registerSeller(request.email(), request.password(), request.displayName(), request.bio());

		assertThat(result.user().getSellerProfile()).isNotNull();
		assertThat(result.user().getSellerProfile().getDisplayName()).isEqualTo(request.displayName());
	}

	@Test
	void login_shouldAuthenticateValidCredentials() {
		UserEntity user = dataFactory.aClientUser();
		user.setPasswordHash(passwordEncoder.encode("Password1!"));
		userRepository.save(user);

		AuthResult result = authService.login(user.getEmail(), "Password1!");

		assertThat(result.token()).isNotBlank();
		assertThat(result.user().getId()).isEqualTo(user.getId());
	}

	@Test
	void login_shouldRejectInvalidCredentials() {
		UserEntity user = dataFactory.aClientUser();
		user.setPasswordHash(passwordEncoder.encode("Password1!"));
		userRepository.save(user);

		assertThatThrownBy(() -> authService.login(user.getEmail(), "WrongPass"))
				.isInstanceOf(ResponseStatusException.class)
				.satisfies(ex -> assertThat(((ResponseStatusException) ex).getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
	}
}
