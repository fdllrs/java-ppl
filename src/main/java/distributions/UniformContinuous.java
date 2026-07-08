package distributions;

import java.util.Random;

public record UniformContinuous(double min, double max) implements Distribution {

	public UniformContinuous {
		Distribution.assertLessThan("uniform-continuous", min, max);
	}

	@Override
	public Object sample(Random rng) {
		return rng.nextDouble() * (max - min) + min;
	}

	@Override
	public double logProb(Object x) {
		Distribution.assertIsNumber(x);
		double val = ((Number) x).doubleValue();
		if (val < min || val > max) {
			return Double.NEGATIVE_INFINITY;
		}
		return -Math.log(max - min);
	}

	@Override
	public String toString() {
		return String.format("(uniform %f %f)", min, max);
	}
}
