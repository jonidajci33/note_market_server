package notes.seller.service.web.system.dto;

public record ConnectivityStatusResponse(
		boolean backendReachable,
		boolean storageEnabled,
		boolean minioReachable,
		String endpoint,
		String bucket,
		String message
) {
}
