package distributions;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class BernoulliTest {

	private final Random rng = new Random(42);

	@Test
	public void testBernoulliSamplingType() {
		Bernoulli bernoulli = new Bernoulli(0.7);
		assertInstanceOf(Boolean.class, bernoulli.sample(rng));
	}

	@Test
	public void testBernoulliLogProbTrue() {
		Bernoulli bernoulli = new Bernoulli(0.7);
		assertEquals(Math.log(0.7), bernoulli.logProb(true), 1e-6);
	}

	@Test
	public void testBernoulliLogProbFalse() {
		Bernoulli bernoulli = new Bernoulli(0.7);
		assertEquals(Math.log(0.3), bernoulli.logProb(false), 1e-6);
	}

	@Test
	public void testBernoulliLogProbInteger1() {
		Bernoulli bernoulli = new Bernoulli(0.7);
		assertEquals(Math.log(0.7), bernoulli.logProb(1), 1e-6);
	}

	@Test
	public void testBernoulliLogProbInteger0() {
		Bernoulli bernoulli = new Bernoulli(0.7);
		assertEquals(Math.log(0.3), bernoulli.logProb(0), 1e-6);
	}

	@Test
	public void testBernoulliThrowsOnNegativeProbability() {
		assertThrows(IllegalArgumentException.class, () -> new Bernoulli(-0.1));
	}

	@Test
	public void testBernoulliThrowsOnProbabilityGreaterThanOne() {
		assertThrows(IllegalArgumentException.class, () -> new Bernoulli(1.1));
	}

	@Test
	public void testBernoulliLogProbThrowsOnNonNumberAndNonBoolean() {
		Bernoulli bernoulli = new Bernoulli(0.7);
		assertThrows(IllegalArgumentException.class, () -> bernoulli.logProb("invalid"));
	}

	@Test
	public void testBernoulliLogProbPZero() {
		Bernoulli b = new Bernoulli(0.0);
		assertEquals(Double.NEGATIVE_INFINITY, b.logProb(true));
		assertEquals(0.0, b.logProb(false), 1e-9);
	}

	@Test
	public void testBernoulliLogProbPOne() {
		Bernoulli b = new Bernoulli(1.0);
		assertEquals(0.0, b.logProb(true), 1e-9);
		assertEquals(Double.NEGATIVE_INFINITY, b.logProb(false));
	}
}

