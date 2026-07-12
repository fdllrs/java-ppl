package distributions;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class NormalTest {

	private final Random rng = new Random(42);

	@Test
	public void testNormalSamplingType() {
		Normal normal = new Normal(10.0, 2.0);
		assertInstanceOf(Double.class, normal.sample(rng));
	}

	@Test
	public void testNormalLogProbAtMean() {
		Normal normal = new Normal(10.0, 2.0);
		double expectedAtMean = -0.5 * ( Math.log(2.0 * Math.PI) ) - Math.log(2.0);
		assertEquals(expectedAtMean, normal.logProb(10.0), 1e-6);
	}

	@Test
	public void testNormalThrowsOnNegativeSigma() {
		assertThrows(IllegalArgumentException.class, () -> new Normal(0.0, -1.0));
	}

	@Test
	public void testNormalLogProbThrowsOnNonNumber() {
		Normal normal = new Normal(10.0, 2.0);
		assertThrows(IllegalArgumentException.class, () -> normal.logProb("not-a-number"));
	}

	@Test
	public void testNormalLogProbSymmetry() {
		Normal normal = new Normal(0.0, 1.0);
		assertEquals(normal.logProb(1.0), normal.logProb(-1.0), 1e-9);
	}

	@Test
	public void testNormalLogProbOffCenter() {
		Normal normal = new Normal(2.0, 3.0);
		double expected = -Math.log(3.0) - 0.5 * Math.log(2.0 * Math.PI);
		assertEquals(expected, normal.logProb(2.0), 1e-9);
	}
}

