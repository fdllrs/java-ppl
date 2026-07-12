package distributions;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class GammaTest {

	private final Random rng = new Random(42);

	@Test
	public void testGammaSamplingType() {
		Gamma gamma = new Gamma(2.0, 2.0);
		assertInstanceOf(Double.class, gamma.sample(rng));
	}

	@Test
	public void testGammaSamplingNonNegative() {
		Gamma gamma = new Gamma(2.0, 2.0);
		assertTrue((Double) gamma.sample(rng) >= 0.0);
	}

	@Test
	public void testGammaLogProb() {
		Gamma gamma = new Gamma(2.0, 2.0);
		double expected = Math.log(0.5 * Math.exp(-1.0));
		assertEquals(expected, gamma.logProb(2.0), 1e-6);
	}

	@Test
	public void testGammaThrowsOnInvalidScale() {
		assertThrows(IllegalArgumentException.class, () -> new Gamma(2.0, 0.0));
	}

	@Test
	public void testGammaLogProbOutOfBounds() {
		Gamma gamma = new Gamma(2.0, 2.0);
		assertEquals(Double.NEGATIVE_INFINITY, gamma.logProb(-1.0));
	}

	@Test
	public void testGammaShapeLessThanOne() {
		Gamma g = new Gamma(0.5, 1.0);
		for (int i = 0; i < 10; i++) {
			assertTrue((double) g.sample(rng) > 0.0);
		}
	}
}

