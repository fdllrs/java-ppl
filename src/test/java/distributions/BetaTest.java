package distributions;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BetaTest {

	private final Random rng = new Random(42);

	@Test
	public void testBetaSamplingType() {
		Beta beta = new Beta(2.0, 2.0);
		assertInstanceOf(Double.class, beta.sample(rng));
	}

	@Test
	public void testBetaSamplingBoundaries() {
		Beta beta = new Beta(2.0, 2.0);
		double sample = (Double) beta.sample(rng);
		assertTrue(sample >= 0.0 && sample <= 1.0);
	}

	@Test
	public void testBetaLogProb() {
		Beta beta = new Beta(2.0, 2.0);
		assertEquals(Math.log(1.5), beta.logProb(0.5), 1e-6);
	}

	@Test
	public void testBetaThrowsOnInvalidAlpha() {
		assertThrows(IllegalArgumentException.class, () -> new Beta(0.0, 2.0));
	}

	@Test
	public void testBetaLogProbOutOfBounds() {
		Beta beta = new Beta(2.0, 2.0);
		assertEquals(Double.NEGATIVE_INFINITY, beta.logProb(1.5));
	}

	@Test
	public void testBetaLogProbSymmetricAtHalf() {
		Beta b = new Beta(1, 1);
		assertEquals(0.0, b.logProb(0.5), 1e-9);
		assertEquals(0.0, b.logProb(0.3), 1e-9);
	}
}

