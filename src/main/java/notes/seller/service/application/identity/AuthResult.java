package notes.seller.service.application.identity;

import notes.seller.service.persistence.identity.UserEntity;

public record AuthResult(String token, UserEntity user) {
}