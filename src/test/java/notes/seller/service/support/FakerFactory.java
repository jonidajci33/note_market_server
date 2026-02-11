package notes.seller.service.support;

import java.util.Random;
import net.datafaker.Faker;

public final class FakerFactory {
	public static final long DEFAULT_SEED = 42L;

	private FakerFactory() {
	}

	public static Faker seeded() {
		return seeded(DEFAULT_SEED);
	}

	public static Faker seeded(long seed) {
		return new Faker(new Random(seed));
	}
}
