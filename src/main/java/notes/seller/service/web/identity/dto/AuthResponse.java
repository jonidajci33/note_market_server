package notes.seller.service.web.identity.dto;

public record AuthResponse(String token, UserResponse user) {
}