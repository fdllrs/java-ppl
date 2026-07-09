package distributions;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ExponentialTest {

	private final Random rng = new Random(42);

	@Test
	public void testExponentialSamplingType() {
		Exponential exp = new Exponential(2.0);
		assertInstanceOf(Double.class, exp.sample(rng));
	}

	@Test
	public void testExponentialSamplingNonNegative() {
		Exponential exp = new Exponential(2.0);
		assertTrue((Double) exp.sample(rng) >= 0.0);
	}

	@Test
	public void testExponentialLogProb() {
		Exponential exp = new Exponential(2.0);
		double expected = Math.log(2.0 * Math.exp(-2.0));
		assertEquals(expected, exp.logProb(1.0), 1e-6);
	}

	@Test
	public void testExponentialThrowsOnInvalidRate() {
		assertThrows(IllegalArgumentException.class, () -> new Exponential(-0.5));
	}

	@Test
	public void testExponentialLogProbOutOfBounds() {
		Exponential exp = new Exponential(2.0);
		assertEquals(Double.NEGATIVE_INFINITY, exp.logProb(-1.0));
	}
}
