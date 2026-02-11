package notes.seller.service.application.identity;

import notes.seller.service.domain.identity.Role;
import notes.seller.service.persistence.identity.SellerProfileEntity;
import notes.seller.service.persistence.identity.UserEntity;
import notes.seller.service.persistence.identity.UserRepository;
import notes.seller.service.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;

	public AuthService(UserRepository userRepository,
					  PasswordEncoder passwordEncoder,
					  AuthenticationManager authenticationManager,
					  JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
	}

	public AuthResult registerClient(String email, String password) {
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}
		UserEntity user = new UserEntity();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(password));
		user.getRoles().add(Role.CLIENT);
		UserEntity saved = userRepository.save(user);
		return new AuthResult(jwtService.generateToken(saved), saved);
	}

	public AuthResult registerSeller(String email, String password, String displayName, String bio) {
		if (userRepository.existsByEmailIgnoreCase(email)) {
			throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
		}
		UserEntity user = new UserEntity();
		user.setEmail(email);
		user.setPasswordHash(passwordEncoder.encode(password));
		user.getRoles().add(Role.SELLER);
		if (displayName != null && !displayName.isBlank()) {
			SellerProfileEntity profile = new SellerProfileEntity();
			profile.setUser(user);
			profile.setDisplayName(displayName);
			profile.setBio(bio);
			user.setSellerProfile(profile);
		}
		UserEntity saved = userRepository.save(user);
		return new AuthResult(jwtService.generateToken(saved), saved);
	}

	public AuthResult login(String email, String password) {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
		} catch (AuthenticationException ex) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
		}
		UserEntity user = userRepository.findByEmailIgnoreCase(email)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
		return new AuthResult(jwtService.generateToken(user), user);
	}
}