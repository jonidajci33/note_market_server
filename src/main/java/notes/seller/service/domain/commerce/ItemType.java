package notes.seller.service.domain.commerce;

public enum ItemType {
	NOTE,
	/** @deprecated Courses feature removed. Retained for JPA deserialization of historic order/entitlement records. */
	@Deprecated
	COURSE
}