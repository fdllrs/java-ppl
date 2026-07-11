package inference;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
