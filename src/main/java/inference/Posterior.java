package inference;

import java.util.Collections;
import java.util.List;

public record Posterior <T extends Number>(List<T> samples, List<Double> weights) {
	public Posterior {
		if (samples.size() != weights.size()) {
			throw new IllegalArgumentException("Samples and weights must have the same size.");
		}
	}

	public static <T extends Number> Posterior<T> ofUnweighted(List<T> samples) {
		if (samples.isEmpty()) {
			return new Posterior<>(samples, Collections.emptyList());
		}
		List<Double> uniformWeights = Collections.nCopies(samples.size(), 1.0 / samples.size());
		return new Posterior<>(samples, uniformWeights);
	}

	public double stdDev() {
		return Math.sqrt(variance());
	}

	public double variance() {
		if (samples.isEmpty()) return 0.0;
		double meanVal = mean();
		double sum = 0.0;
		for (int i = 0; i < samples.size(); i++) {
			double diff = samples.get(i).doubleValue() - meanVal;
			sum += diff * diff * weights.get(i);
		}
		return sum;
	}

	public double mean() {
		if (samples.isEmpty()) return 0.0;
		double sum = 0.0;
		for (int i = 0; i < samples.size(); i++) {
			sum += samples.get(i).doubleValue() * weights.get(i);
		}
		return sum;
	}

	public double effectiveSampleSize() {
		if (weights.isEmpty()) return 0.0;
		double sumWeights = 0.0;
		double sumSquaredWeights = 0.0;
		for (double w : weights) {
			sumWeights += w;
			sumSquaredWeights += w * w;
		}
		if (sumSquaredWeights == 0.0) return 0.0;
		return ( sumWeights * sumWeights ) / sumSquaredWeights;
	}
}
