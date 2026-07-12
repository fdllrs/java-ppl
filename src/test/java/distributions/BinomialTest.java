package distributions;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BinomialTest {

	private final Random rng = new Random(42);

	@Test
	public void testBinomialSamplingType() {
		Binomial binomial = new Binomial(5, 0.4);
		assertInstanceOf(Integer.class, binomial.sample(rng));
	}

	@Test
	public void testBinomialLogProbSuccess() {
		Binomial binomial = new Binomial(5, 0.4);
		double expected = Math.log(
				10 * 0.16 * 0.216); // P(X=2) = (5 choose 2) * 0.4^2 * 0.6^3 = 0.3456
		assertEquals(expected, binomial.logProb(2), 1e-6);
	}

	@Test
	public void testBinomialThrowsOnNegativeTrials() {
		assertThrows(IllegalArgumentException.class, () -> new Binomial(-1, 0.5));
	}

	@Test
	public void testBinomialThrowsOnInvalidProbability() {
		assertThrows(IllegalArgumentException.class, () -> new Binomial(5, 1.5));
	}

	@Test
	public void testBinomialLogProbOutOfBoundsAbove() {
		Binomial binomial = new Binomial(5, 0.4);
		assertEquals(Double.NEGATIVE_INFINITY, binomial.logProb(6));
	}

	@Test
	public void testBinomialLogProbOutOfBoundsBelow() {
		Binomial binomial = new Binomial(5, 0.4);
		assertEquals(Double.NEGATIVE_INFINITY, binomial.logProb(-1));
	}

	@Test
	public void testBinomialLogProbNonIntegerIsNegInf() {
		Binomial b = new Binomial(5, 0.5);
		assertEquals(Double.NEGATIVE_INFINITY, b.logProb(2.7));
	}
}

