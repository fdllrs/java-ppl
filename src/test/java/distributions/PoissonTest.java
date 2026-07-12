package distributions;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class PoissonTest {

	private final Random rng = new Random(42);

	@Test
	public void testPoissonSamplingType() {
		Poisson poisson = new Poisson(3.5);
		assertInstanceOf(Integer.class, poisson.sample(rng));
	}

	@Test
	public void testPoissonSamplingLargeLambda() {
		Poisson poissonLarge = new Poisson(45.0);
		assertInstanceOf(Integer.class, poissonLarge.sample(rng));
		// Draw multiple samples to ensure the rejection sampler executes correctly without
		// infinite loops
		for (int i = 0; i < 100; i++) {
			int val = (Integer) poissonLarge.sample(rng);
			assertTrue(val >= 0);
		}
	}

	@Test
	public void testPoissonLogProbSuccess() {
		Poisson poisson = new Poisson(3.0);
		// P(X=0) = e^-3
		assertEquals(-3.0, poisson.logProb(0), 1e-9);

		// P(X=2) = (3^2 * e^-3) / 2 = 4.5 * e^-3
		double expected = Math.log(4.5) - 3.0;
		assertEquals(expected, poisson.logProb(2), 1e-9);
	}

	@Test
	public void testPoissonThrowsOnNonPositiveLambda() {
		assertThrows(IllegalArgumentException.class, () -> new Poisson(0.0));
		assertThrows(IllegalArgumentException.class, () -> new Poisson(-1.5));
	}

	@Test
	public void testPoissonLogProbOutOfBoundsBelow() {
		Poisson poisson = new Poisson(3.0);
		assertEquals(Double.NEGATIVE_INFINITY, poisson.logProb(-1));
		assertEquals(Double.NEGATIVE_INFINITY, poisson.logProb(-5));
	}

	@Test
	public void testPoissonLogProbNonIntegerIsNegInf() {
		Poisson poisson = new Poisson(3.0);
		assertEquals(Double.NEGATIVE_INFINITY, poisson.logProb(2.5));
	}
}
