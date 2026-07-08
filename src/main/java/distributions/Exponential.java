package distributions;

import java.util.Random;

public record Exponential(double rate) implements Distribution {

	public Exponential {
		Distribution.assertPositive("exponential", rate);
	}

	@Override
	public Object sample(Random rng) {
		double uniform = rng.nextDouble();
		return -Math.log(uniform) / rate;
	}

	@Override
	public double logProb(Object x) {
		Distribution.assertIsNumber(x);
		double val = ((Number) x).doubleValue();
		if (val < 0.0) {
			return Double.NEGATIVE_INFINITY;
		}
		return Math.log(rate) - rate * val;
	}

	@Override
	public String toString() {
		return String.format("(exponential %f)", rate);
	}
}
