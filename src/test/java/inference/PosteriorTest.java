package inference;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PosteriorTest {

	@Test
	public void testUnweightedEffectiveSampleSize() {
		List<Integer> samples = List.of(1, 2, 3, 4, 5);
		Posterior<Integer> posterior = Posterior.ofUnweighted(samples);
		assertEquals(5.0, posterior.effectiveSampleSize(), 1e-6);
	}

	@Test
	public void testWeightedEffectiveSampleSize() {
		List<Integer> samples = List.of(1, 2);
		List<Double> weights = List.of(0.1, 0.9);
		Posterior<Integer> posterior = new Posterior<>(samples, weights);

		// ESS = (0.1 + 0.9)^2 / (0.1^2 + 0.9^2) = 1.0 / (0.01 + 0.81) = 1.0 / 0.82
		double expectedESS = 1.0 / 0.82;
		assertEquals(expectedESS, posterior.effectiveSampleSize(), 1e-6);
	}

	@Test
	public void testEmptyPosteriorEffectiveSampleSize() {
		Posterior<Integer> posterior = Posterior.ofUnweighted(List.of());
		assertEquals(0.0, posterior.effectiveSampleSize(), 1e-6);
	}

	@Test
	public void testConstructorRejectsUnequalSizes() {
		assertThrows(IllegalArgumentException.class,
					 () -> new Posterior<>(List.of(1), List.of(0.5, 0.5)));
	}

	@Test
	public void testEmptyPosteriorIsAllowed() {
		Posterior<Double> p = new Posterior<>(List.of(), List.of());
		assertTrue(p.samples().isEmpty());
		assertTrue(p.weights().isEmpty());
	}

	@Test
	public void testOfUnweightedProducesUniformWeights() {
		Posterior<Double> p = Posterior.ofUnweighted(List.of(1.0, 2.0, 3.0, 4.0));
		double expected = 1.0 / 4.0;
		for (double w : p.weights()) {
			assertEquals(expected, w, 1e-12);
		}
	}

	@Test
	public void testOfUnweightedWeightsSumToOne() {
		Posterior<Double> p = Posterior.ofUnweighted(List.of(1.0, 2.0, 3.0));
		double sum = p.weights().stream().mapToDouble(Double::doubleValue).sum();
		assertEquals(1.0, sum, 1e-12);
	}

	@Test
	public void testMeanOnEmptyReturnsZero() {
		Posterior<Double> p = new Posterior<>(List.of(), List.of());
		assertEquals(0.0, p.mean(), 1e-12);
	}

	@Test
	public void testMeanUniform() {
		Posterior<Double> p = Posterior.ofUnweighted(List.of(1.0, 2.0, 3.0));
		assertEquals(2.0, p.mean(), 1e-12);
	}

	@Test
	public void testMeanWeighted() {
		Posterior<Double> p = new Posterior<>(List.of(0.0, 1.0), List.of(0.9, 0.1));
		assertEquals(0.1, p.mean(), 1e-12);
	}

	@Test
	public void testVarianceOnEmptyReturnsZero() {
		Posterior<Double> p = new Posterior<>(List.of(), List.of());
		assertEquals(0.0, p.variance(), 1e-12);
	}

	@Test
	public void testVarianceConstantSamples() {
		Posterior<Double> p = Posterior.ofUnweighted(List.of(5.0, 5.0, 5.0));
		assertEquals(0.0, p.variance(), 1e-12);
	}

	@Test
	public void testVarianceUniform() {
		Posterior<Double> p = Posterior.ofUnweighted(List.of(0.0, 1.0));
		assertEquals(0.25, p.variance(), 1e-12);
	}

	@Test
	public void testStdDevIsSquareRootOfVariance() {
		Posterior<Double> p = Posterior.ofUnweighted(List.of(0.0, 1.0));
		assertEquals(Math.sqrt(p.variance()), p.stdDev(), 1e-12);
	}
}

